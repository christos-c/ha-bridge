

module.exports = {

  rgbToHsv: function rgbToHsv(r, g, b) {
    r /= 0xFF;
    g /= 0xFF;
    b /= 0xFF;
    var max = Math.max(r, g, b), min = Math.min(r, g, b);
    var h, s, v = max;

    var d = max - min;
    s = max == 0 ? 0 : d / max;

    if (max == min) {
      h = 0;
    }
    else {
      switch (max) {
        case r:
          h = ((g - b) / d + (g < b ? 6 : 0)); break;
        case g:
          h = ((b - r) / d + 2); break;
        case b:
          h = ((r - g) / d + 4); break;
      }
      h = Math.round(h * 60);
      s = Math.round(s * 100);
      v = Math.round(v * 100);
    }
    return [h, s, v];
  },

  hsvToMilightColor:  function hsvToMilightColor(hsv) {
    // On the HSV color circle (0..360) with red at 0 degree. We need to convert to the Milight color circle
    // which has 256 values with red at position 176
    return (256 + 176 - Math.floor(Number(hsv[0]) / 360.0 * 255.0)) % 256;
  },

  rgbToHue: function rgbToMilightHue(r, g, b) {
    // On the HSV color circle (0..360) the hue value start with red at 0 degrees. We need to convert this
    // to the Milight color circle which has 256 values with red at position 176
    var hsv = this.rgbToHsv(r, g, b);
    return (256 + 176 - Math.floor(Number(hsv[0]) / 360.0 * 255.0)) % 256;
  },

  buffer2hex: function buffer2hex(buffer) {
    var result = [];
    for (var i = 0; i < buffer.length; i++) {
      result.push('0x' + ('0' + (buffer[i]).toString(16)).slice(-2).toUpperCase())
    }
    return result;
  },

  debug: process.env.hasOwnProperty('MILIGHT_DEBUG')? consoleDebug : function () {},

  settlePromise: function settlePromise(aPromise) {
    return aPromise.reflect();
  },

  settlePromises: function settlePromises(promisesArray) {
    return Promise.all(promisesArray.map(function(promise) {
      return promise.reflect()
    }));
  }
};

function consoleDebug() {
  if (typeof arguments[0] == 'string') {
    arguments[0] = 'Milight: ' + arguments[0]
  }
  console.log.apply(this, arguments)
}