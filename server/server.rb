#!/usr/bin/env ruby
require 'optparse'
require 'json'
require 'scanf'
require 'set'
require 'socket'
require 'pqueue'

trap("SIGINT") { exit!(0) }

PORT = 8080

options = {}
parser = OptionParser.new do|opts|
  opts.banner = "Usage: ./server.rb [options]"

  opts.on('-m', '--map map', 'map path') do |map|
    options[:map] = map
  end

  opts.on('-u', '--users users', 'Users count (2..10), 2 by default') do |users|
    options[:users] = users.to_i
  end

  opts.on('-h', '--help', 'Displays Help') do
    puts opts
    exit
  end
end

parser.parse!
options[:users] ||= 2

if !options[:map]
  puts "Map parameter is missing"
  print parser.help
  exit(1)
end

if !File.exists?(options[:map])
  puts "Map not found: #{options[:map]}"
  exit(1)
end

if options[:users] < 2 || options[:users] > 10
  puts "Invalid user count: #{options[:users]}"
  exit(1)
end


class Graph
  INF = 1e9
  attr_reader :edges, :vertices, :edges_count

  def initialize
    @edges = Hash.new { |h,k| h[k] = Set.new }
    @scores = {}
    @vertices = Set.new
    @edges_count = 0
  end

  def add_edge(from ,to)
    @edges_count += 1
    @edges[from] << to
    @edges[to] << from
    @vertices << from
    @vertices << to
    nil
  end

  def has_edge?(from, to)
    @edges[from].include?(to)
  end

  def adjacents(u)
    @edges[u]
  end

  def score(mine, points)
    score = 0

    if points.include?(mine)
      points.map { |point| scores(mine)[point]**2 }.reduce(:+)
    else
      0
    end
  end

  def scores(mine)
    @scores[mine] ||= dijkstra(mine)
  end

private
  def dijkstra(u)
    d = Hash.new { |h,k| h[k] = INF }
    d[u] = 0

    pq = PQueue.new { |(a,_),(b, _)| a < b }
    pq.push([0, u])

    while !pq.empty?
      cur_d, v  = pq.pop
      next if cur_d > d[v]

      edges[v].each do |to|
        if d[v] + 1 < d[to]
          d[to] = d[v] + 1
          pq.push([d[to], to])
        end
      end
    end

    d.reject {|e| e == INF }
  end
end

class Matrix
  def initialize(punters)
    @claims  = Hash.new {|h,k| h[k] = {} }
    @punter_vertices = Array.new(punters) { Set.new }
    @allowed = Array.new(punters) { Set.new  }
    @graph = Graph.new
  end

  def add_edge(from, to)
    @graph.add_edge(from, to)
  end

  def claim(source, target, punter)
    if source > target
      source, target = target, source
    end

    if @graph.has_edge?(source, target) &&      # adj exists
       !@claims[source][target] &&              # adj is free
       (@allowed[punter].empty? || @allowed[punter].include?(source) || @allowed[punter].include?(target)) # punter's net includes source or target

      @claims[source][target] = punter
      @punter_vertices[punter] << source
      @punter_vertices[punter] << target
      @allowed[punter] = @allowed[punter] | @graph.adjacents(source) | @graph.adjacents(target)

      true
    end
  end

  def edges_count
    @edges_count ||= @graph.edges_count
  end

  def mines(mines)
    @mines = Set.new(mines)
  end

  def score_for(punter)
    score = 0

    @mines.each do |mine|
      score += @graph.score(mine, @punter_vertices[punter])
    end

    score
  end
end

class Game
  attr_reader :state, :map, :moves

  def initialize(map, nplayer)
    @state = {}
    @map = map
    @matrix = parse_map(map, nplayer)
    @edges_count = @matrix.edges_count
    @occupied = 0
    @moves = []
  end

  def move(move)
    unless move["pass"]
      claim = move["claim"]
      if @matrix.claim(claim["source"], claim["target"], claim["punter"])
        @occupied += 1
        @moves << {claim: claim}
      end
    end
  end

  def result(punter)
    @game.calc_result(punter)
  end

  def finished?
    p @occupied
    p @edges_count
    @occupied == @edges_count
  end

  def scores(punters)
    @scores ||= calc_scores(punters)
  end

  def finish!
    @occupied = @edges_count
  end

private
  def parse_map(map, nplayer)
    matrix = Matrix.new(nplayer)
    matrix.mines(map["mines"])
    map["rivers"].each do |river|
      matrix.add_edge(river["source"], river["target"])
    end
    matrix
  end

  def calc_scores(punters)
    (0..punters-1).map do |punter|
      {punter: punter, score: @matrix.score_for(punter)}
    end
  end
end

class Server
  attr_reader :server, :game, :nclients, :clients

  def initialize(game, port, nclients)
    @server = TCPServer.open(port)
    @clients = []
    @game = game
    @nclients = nclients
  end

  def run
    while clients.size < nclients
      client = server.accept
      begin
        msg = read(client)

        if msg["me"].length > 0
          write(client, you: msg["me"])
          clients << client
        else
          client.close
        end
      rescue Exception => e
        p e
      end
    end

    clients.each_with_index do |client, i|
      write(client, punter: i, punters: nclients, map: game.map)
      msg = read(client)
      if msg["ready"] != i
        puts "client #{i} is not ready"
        clear
        return
      end
    end

    while !game.finished?
      clients.each_with_index do |client, i|
        unless client.closed?
          break if game.finished?
          write(client, move: {moves: game.moves})
          msg = read(client)
          game.move(msg)
        end
      end
    end

    clients.each do |client|
      unless client.closed?
        write(client, stop: {moves: game.moves, scores: game.scores(nclients)})
      end
    end
  end

  def clear
    @server.close
    @clients.each {|c| c.close}
  end

private
  def read(client)
    len = ""
    c = client.read(1)
    while c != ':'
      len += c
      c = client.read(1)
    end
    msg = client.read(len.to_i)
    p msg
    JSON.parse(msg)
  # rescue Exception => e
  #   p e
  #   clear
  #   game.finish!
  end

  def write(client, msg)
    msg = msg.to_json
    puts "outgoing: #{msg}"
    client.printf("%d:%s\n", msg.length, msg)
  end
end


loop do
  map = JSON.parse(IO.read(options[:map]))
  game = Game.new(map, options[:users])
  server = Server.new(game, PORT, options[:users])
  server.run
  server.clear
end
