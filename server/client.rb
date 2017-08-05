require 'json'
require 'socket'
require 'scanf'

greeting = {me: Time.now.to_i.to_s}.to_json #{}"Lambda Riot"}.to_json

# URL = "punter.inf.ed.ac.uk"
# PORT = 9005

URL = "localhost"
PORT = 8080

def parse_state(json)
  JSON.parse(json)

end

class Game
  NAME = "Lambda Riot"

  attr_reader :server, :punter, :punters

  def initialize(server)
    @server = server
    @name = Time.now.to_i.to_s
  end

  def run
    r = 0
    server.ssend(me: @name)
    msg = server.read
    raise msg.inspect if msg["you"] != @name
    #p msg
    loop do
      msg = server.read
      #p msg
      if msg["map"]
        parse_state(msg)
      elsif msg["move"]
        move(msg)
      elsif msg["stop"]
        r = result(msg)
        break
      else
        puts "Unknown msg: #{msg}"
        raise msg.inspect
      end
    end

    @server.disconnect!
    r
  end

  def parse_state(msg)
    @punter = msg["punter"]
    @punters = msg["punters"]
    @map = msg["map"]
    server.ssend(ready: punter)
  end

  def move(msg)
    @server.ssend(pass: {punter: punter})
  end

  def result(msg)
    msg["stop"]["scores"].detect {|e| e["punter"] == punter}["score"]
  end
end


class Server
  def initialize(url, port)
    @url = url
    @port = port
    @server = TCPSocket.open(url, port)
  end

  def ssend(msg)
    msg = msg.to_json
    puts msg
    @server.printf("%d:%s", msg.length, msg)
  end

  def read
    len = @server.scanf("%d:%s")
    puts len[1]
    JSON.parse(len[1])
  end

  def disconnect!
    @server.close
  end
end

    server = Server.new(URL, PORT)
    game = Game.new(server)
    game.run
