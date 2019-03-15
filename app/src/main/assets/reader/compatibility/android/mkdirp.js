var MKDirPCompatibility = function MKDirPCompatibility() {};

MKDirPCompatibility.sync = function (path) {
  app.mkdirs(path);
};