
document.addEventListener('DOMContentLoaded', function(){ 
  var textarea = document.getElementById("code")
  var cm = CodeMirror.fromTextArea(textarea, {
    lineNumbers:true,
    // mode:"clike",
    mode:"text/x-scala",
        "autofocus": true,
        "gutters": ["CodeMirror-linenumbers"],
        "lineWrapping": false,
        "tabSize": 2,
        "indentWithTabs": false,
        // "theme": "solarized dark",
        // "theme": "ambiance",
        "theme": "material",
        "smartIndent": true,
        "keyMap": "sublime",
        "scrollPastEnd": false,
        // "scrollbarStyle": "simple",
        "autoCloseBrackets": true,
        "matchBrackets": true,
        "showCursorWhenSelecting": true,
        // "highlightSelectionMatches": {
        //   "showToken" -> js.Dynamic.global.RegExp("\\w")),
        // }
        "extraKeys" : {
        //   "Tab" : "defaultTab",
          "cmd-Enter" : function(cm){ console.log("run")}, //???
        //   ctrl + "-S" : "save",
        //   ctrl + "-M" : "newSnippet",
        //   "Ctrl" + "-Space" : "autocomplete",
        //   "Esc" : "clear",
        //   "F1" : "help",
        //   "F2" : "toggleSolarized",
        //   "F3" : "toggleConsole",
        //   "F4" : "toggleWorksheet",
        //   "F6" : "formatCode",
        //   "F7" : "toggleLineNumbers",
        //   "F8" : "togglePresentationMode"
        }
      
  })
  cm.setSize("100%", "100%")

  cm.on("keyHandled", function(cm,name,event){ if(name == "Cmd-Enter") com.fishuyo.Main().send(cm.getValue()) })

  setInterval(function(){ com.fishuyo.Main().send("keepalive"); }, 1000);

    // Initialize collapse button
  $('.button-collapse').sideNav({
      menuWidth: 300, // Default is 300
      // edge: 'right', // Choose the horizontal origin
      // closeOnClick: true, // Closes side-nav on <a> clicks, useful for Angular/Meteor
      // draggable: true, // Choose whether you can drag to open on touch screens,
      // onOpen: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is opened
      // onClose: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is closed
    }
  );
  // Initialize collapsible (uncomment the line below if you use the dropdown variation)
  $('.collapsible').collapsible();
}, false);



