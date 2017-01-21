var Milight = require('../index').MilightController;
var commands = require('../index').commandsV6;
var args = process.argv.slice(2);

var light = new Milight({ ip: "192.168.1.8", type: 'v6' });
var zone = args[0];
var colour = args[1];
console.log("Zone " + zone);
if (colour == 0)
    light.sendCommands(commands.rgbw.on(zone), commands.rgbw.whiteMode(zone));
else
    light.sendCommands(commands.rgbw.on(zone), commands.rgbw.hue(zone, colour));
light.pause(1000);

light.close().then(function () {
  console.log("All command have been executed - closing Milight");
});
