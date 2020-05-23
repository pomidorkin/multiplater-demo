const http = require('http')
const express = require('express')
const socketio = require('socket.io')
const players = []

const app = express()
const server = http.createServer(app)
const io = socketio(server)

server.listen(3000, () => {
    console.log('Server is now running')
})

io.on('connection', (socket) => {
    console.log('Player connected')
    socket.emit('socketID', {id: socket.id})
    socket.emit('getPlayers', players)
    socket.broadcast.emit('newPlayer', {id: socket.id})
    socket.on('playerMoved', (data) => {
        // Data already contins x and y, so here we add id to the data object
        data.id = socket.id
        socket.broadcast.emit('playerMoved', data)

        for(i = 0; i < players.length; i++){
            if(players[i].id == data.id){
                players[i].x = data.x
                players[i].y = data.y
            }
        }
    })

    socket.on('disconnect', () => {
        console.log('Player disconnected')
        socket.broadcast.emit('playerDisconnected', {id: socket.id})
        for(i = 0; i < players.length; i++){
            if(players[i].id == socket.id){
                players.splice(i, 1)
                console.log(players)
            }
        }
    })
    players.push(new player(socket.id, 0, 0))
})

function player(id, x, y){
    this.id = id
    this.x = x
    this.y = y
}