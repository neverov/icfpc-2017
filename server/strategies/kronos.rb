require 'set'
require 'pqueue'

class PlayGraph
  INF = 1e9

  attr_reader :allowed, :mines, :busy

  def initialize(initial_state)
    @mines   = initial_state["map"]["mines"]
    @punters = initial_state["map"]["punters"]
    @punter  = initial_state["map"]["punter"]
    @edges = Hash.new { |h,k| h[k] = Set.new }
    @vertices = Set.new(initial_state["map"]["sites"].map {|site| site["id"]})
    @allowed = Set.new

    initial_state["map"]["rivers"].each do |river|
      add_edge(river["source"], river["target"])
    end

    @busy = Hash.new { |h, k| h[k] = Set.new }
    @paths = {}
  end

  def add_edge(from ,to)
    @edges[from] << to
    @edges[to] << from
  end

  def has_edge?(from, to)
    @edges[from].include?(to)
  end

  def adjacents(u)
    @edges[u]
  end

  def claim(s, t)
    mark_as_busy(s, t)
    @allowed = @allowed | adjacents[s] | adjacents[t]
  end

  def mark_as_busy(source, target)
    busy[source] << target
    busy[target] << busy
  end

  def busy?(source, target)
    busy[source].include?(target)
  end

  def dijkstra(u)
    @paths[u] if @paths[u]

    d = Hash.new { |h,k| h[k] = INF }
    d[u] = 0

    pq = PQueue.new { |(a,_),(b, _)| a < b }
    pq.push([0, u])

    while !pq.empty?
      cur_d, v  = pq.pop
      next if cur_d > d[v]

      @edges[v].each do |to|
        if d[v] + 1 < d[to]
          d[to] = d[v] + 1
          pq.push([d[to], to])
        end
      end
    end

    @paths[u] = d.reject {|e| e == INF }
  end
end

class KronosStrategy
  attr_reader :graph, :punter

  def initialize(punter, initial_state)
    @punter = punter
    @last_step = -1
    @my_move = 0
    @graph = PlayGraph.new(initial_state)
  end

  def move(moves)
    # moves = moves.reject { |move| move["pass"] }

    # moves[(@last_step + 1)...moves.size].each do |move|
    #   if move["punter"] != punter
    #     graph.mark_as_busy(move["source"], move["target"])
    #   end
    # end

    # @last_step = moves.size

    # if @my_move == 0
    #   source, target = find_best_mine
    # else
    #   source, target = occupy_nearby_mine
    #   unless source
    #     source, target = find_best_move
    #   end
    # end

    # @my_move += 1

    # if source.nil? || target.nil?
    #   raise "Bad move: #{source} #{target}"
    # end

    # graph.claim(source, target)
    # format_move(source, target)
    {pass: {punter: punter}}
  end

private
  def find_best_move
    mines_values = {}

    graph.mines.each do |mine|
      mines_values[mine] = {busy_edges: 0, edges: [], distance: 0, mine: mine}
      graph.adjacents(mine).each do |v|
        if graph.busy?(mine, v)
          mines_values[mine][:busy_edges] += 1
        else
          mines_values[mine][:edges] << v
        end
      end
    end

    len = graph.mines.size - 1

    mines_values.values.each do |mine_f|
      mines_values.values.each do |mine_t|
        mines_values[mine_f[:mine]][:distance] += graph.dijkstra(mine_f[:mine])[mine_t[:mine]]
      end
    end

    mines_value = mines_values.values.reject {|mine| mine[:edges].empty? }.sort_by {|mine| mine[:distance]}.first
    [mines_value[:mine], mines_value[:edges].first]
  end

  def occupy_nearby_mine
    # TODO
  end

  def find_best_mine
    # TODO
  end

  def format_move(source, target)
    {claim: {punter: punter, source: source, target: target}}
  end
end
