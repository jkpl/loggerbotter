# loggerbotter

A (toy) Clojure app for monitoring IRC connections and logging the incoming
data. The data is presented via a web interface in using WebSockets.

## Technologies used

* Server
    * [Aleph][] for IRC connections
    * [Lamina][] for nice dataflow stuff
    * [Clutch][] for saving log and meter data to [CouchDB][].
    * [HTTP Kit][] for serving the data via [WebSocket][]
* Client
    * [Yeoman][], [Bower][], and [Grunt][] for client side
    development and building.
    * [Bootstrap][] for UI widgets
    * [RxJS][] for FRP needs
    * [Knockout.JS][] for binding data to UI
    * [Highcharts][] for drawing nice charts

## Future development

* Make the app more error resistant
* Add more meters for measuring more data, and Improve existing meters
* Add more tests for both server and client
* Make client side code more moduler using [Browserify][] or something similar.
* Add support for other protocols than IRC

## License

Copyright © 2014 Jaakko Pallari

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[aleph]: https://github.com/ztellman/aleph
[lamina]: https://github.com/ztellman/lamina
[clutch]: https://github.com/clojure-clutch/clutch
[couchdb]: https://couchdb.apache.org/
[http kit]: http://http-kit.org/
[websocket]: https://en.wikipedia.org/wiki/WebSocket
[yeoman]: http://yeoman.io/
[bower]: http://bower.io/
[grunt]: http://gruntjs.com/
[bootstrap]: http://getbootstrap.com/
[rxjs]: https://reactive-extensions.github.io/RxJS/
[knockout.js]: http://knockoutjs.com/
[highcharts]: http://www.highcharts.com/
[browserify]: http://browserify.org/
