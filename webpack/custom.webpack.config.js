var merge = require('webpack-merge').merge;
var generated = require('./scalajs.webpack.config');
// const UnoCSS = require('@unocss/webpack').default //


var local = {
    output: {hashFunction : "sha512"},
    devServer: {
        historyApiFallback: true
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(ttf|eot|woff|png|glb|svg)$/,
                use: 'file-loader'
            },
            {
                test: /\.(eot)$/,
                use: 'url-loader'
            },
            {
                test: /\.(js|mjs)$/,
                resolve: {
                    fullySpecified: false
                }
            }
        ]
    },
    // plugins: [ //
    //     UnoCSS()
    // ],
    // css: {
    //     extract: {
    //         filename: '[name].[hash:9].css'
    //     }
    // }
};

module.exports = merge(generated, local);
