var Milight = require('../index').MilightController;
var commands = require('../index').commandsV6;
var args = process.argv.slice(2);

var light = new Milight({ ip: "192.168.1.8", type: 'v6' });
var zone = args[0];
console.log("Zone " + zone);
light.sendCommands(commands.rgbw.on(zone));
light.pause(1000);

light.close().then(function () {
  console.log("All command have been executed - closing Milight");
});
