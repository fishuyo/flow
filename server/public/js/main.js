$(function(){


//   var textarea = document.getElementById("code")
//   // var cm = CodeMirror.fromTextArea(textarea, {
//   //   lineNumbers:true,
//   //   // mode:"clike",
//   //   mode:"text/x-scala",
//   //       "autofocus": true,
//   //       "gutters": ["CodeMirror-linenumbers"],
//   //       "lineWrapping": false,
//   //       "tabSize": 2,
//   //       "indentWithTabs": false,
//   //       // "theme": "solarized dark",
//   //       // "theme": "ambiance",
//   //       "theme": "material",
//   //       "smartIndent": true,
//   //       "keyMap": "sublime",
//   //       "scrollPastEnd": false,
//   //       // "scrollbarStyle": "simple",
//   //       "autoCloseBrackets": true,
//   //       "matchBrackets": true,
//   //       "showCursorWhenSelecting": true,
//   //       // "highlightSelectionMatches": {
//   //       //   "showToken" -> js.Dynamic.global.RegExp("\\w")),
//   //       // }
//   //       "extraKeys" : {
//   //       //   "Tab" : "defaultTab",
//   //         "cmd-Enter" : function(cm){ console.log("run")}, //???
//   //       //   ctrl + "-S" : "save",
//   //       //   ctrl + "-M" : "newSnippet",
//   //       //   "Ctrl" + "-Space" : "autocomplete",
//   //       //   "Esc" : "clear",
//   //       //   "F1" : "help",
//   //       //   "F2" : "toggleSolarized",
//   //       //   "F3" : "toggleConsole",
//   //       //   "F4" : "toggleWorksheet",
//   //       //   "F6" : "formatCode",
//   //       //   "F7" : "toggleLineNumbers",
//   //       //   "F8" : "togglePresentationMode"
//   //       }
      
//   // })
//   // cm.setSize("100%", "100%")

//   // cm.on("keyHandled", function(cm,name,event){ if(name == "Cmd-Enter") flow.Main().send(cm.getValue()) })

//   // setInterval(function(){ flow.Socket().send("keepalive"); }, 1000);

    // Initialize collapse button
  $('#menu-button-left').sideNav({
      menuWidth: 300, // Default is 300
      // edge: 'right', // Choose the horizontal origin
      // closeOnClick: true, // Closes side-nav on <a> clicks, useful for Angular/Meteor
      // draggable: true, // Choose whether you can drag to open on touch screens,
      // onOpen: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is opened
      // onClose: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is closed
    }
  );
  $('#menu-button-right').sideNav({
      menuWidth: 300, // Default is 300
      edge: 'right', // Choose the horizontal origin
      // closeOnClick: true, // Closes side-nav on <a> clicks, useful for Angular/Meteor
      // draggable: true, // Choose whether you can drag to open on touch screens,
      // onOpen: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is opened
      // onClose: function(el) { /* Do Stuff */ }, // A function to be called when sideNav is closed
    }
  );
  // Initialize collapsible (uncomment the line below if you use the dropdown variation)
  $('.collapsible').collapsible();

  // $('.modal').modal();
  $('.modal').modal({
      // dismissible: true, // Modal can be dismissed by clicking outside of the modal
      // opacity: .5, // Opacity of modal background
      // inDuration: 300, // Transition in duration
      // outDuration: 200, // Transition out duration
      // startingTop: '4%', // Starting top style attribute
      // endingTop: '10%', // Ending top style attribute
      ready: function(modal, trigger) { // Callback for Modal open. Modal and trigger parameters available.
        $('input:text:visible:first').focus();
      },
      // complete: function() { alert('Closed'); } // Callback for Modal close
    }
  );

});




