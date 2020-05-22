const http = require('http')
const express = require('express')
const socketio = require('socket.io')

const app = express()
const server = http.createServer(app)
const io = socketio(server)

server.listen(3000, () => {
    console.log('Server is now running')
})

io.on('connection', (socket) => {
    console.log('Player connected')

    socket.on('disconnect', () => {
        console.log('Player disconnected')
    })
})