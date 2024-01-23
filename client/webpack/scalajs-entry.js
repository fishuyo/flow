require('virtual:windi.css')
// require('virtual:windi-devtools')

if (process.env.NODE_ENV === "production") {
    const opt = require("./client-opt.js");
    opt.main();
    module.exports = opt;
} else {
    var exports = window;
    exports.require = require("./client-fastopt-entrypoint.js").require;
    window.global = window;

    const fastOpt = require("./client-fastopt.js");
    fastOpt.main()
    module.exports = fastOpt;

    if (module.hot) {
        module.hot.accept();
    }
}
