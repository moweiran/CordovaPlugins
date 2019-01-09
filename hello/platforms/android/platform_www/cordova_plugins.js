cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "coolPlugin.CoolPlugin",
    "file": "plugins/coolPlugin/www/CoolPlugin.js",
    "pluginId": "coolPlugin",
    "clobbers": [
      "cool.toast"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-whitelist": "1.3.3",
  "coolPlugin": "0.0.1"
};
// BOTTOM OF METADATA
});