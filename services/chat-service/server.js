const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: {
    origin: '*'
  }
});

app.use(express.json());

io.on('connection', socket => {
  console.log('Client connected');

  socket.on('join', room => {
    socket.join(room);
    console.log(`Client joined room ${room}`);
  });

  socket.on('disconnect', () => {
    console.log('Client disconnected');
  });
});

// Endpoint for WhatsApp style JSON
app.post('/wsp', (req, res) => {
  const body = req.body;

  const waId = body.WaId || (body.From || '').replace('whatsapp:', '');
  const payload = {
    from: waId,
    text: body.Body || '',
    timestamp: Date.now().toString()
  };
  io.to(waId).emit('message', payload);

  res.status(200).json({ status: 'received' });
});

// Endpoint for executive JSON
app.post('/executive', (req, res) => {
  const { mensaje, numeroTelefonoEmpresa, nombreCompletoEjecutivo, numeroTelefonoCliente } = req.body;
  const payload = { mensaje, numeroTelefonoEmpresa, nombreCompletoEjecutivo, numeroTelefonoCliente };
  io.to(numeroTelefonoCliente).emit('message', payload);
  res.status(200).json({ status: 'sent' });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Chat service listening on port ${PORT}`);
});