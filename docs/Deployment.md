# Host Plugins

## Install/Update

1. Build a ZIP archive of the host plugin;

2. Place the ZIP archive on the folder referenced on the property plugins.path of application.yml;

3. If the host plugin requires properties, they may be defined on application.yml, check its
   reference for more details;

4. (Re)Launch the node. It should appear in logs one line mentioning it loaded the plugin.