// package flow

// import slinky.core._
// import slinky.core.annotations.react
// import slinky.web.html._
// import slinky.core.facade.Hooks._

// import scala.scalajs.js
// import scala.scalajs.js.annotation.JSImport

// // import typings.react.reactStrings.submit
// import typings.semanticUiReact.distCommonjsGenericMod.{SemanticICONS, SemanticSIZES, SemanticWIDTHSSTRING}
// import typings.semanticUiReact.semanticUiReactStrings.{submit, left}
// import typings.semanticUiReact.{components => Sui}

// import scala.language.implicitConversions

// @react object FlowUI {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>

//     // useEffect(() => {Boids.main(Array())} )
//     div(
//       // Header(),
//       // Content(),
//       // Footer(),
//     )
    
//   }
// }


// @react object Test {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>
//     val (isModalVisible, updateIsModalVisible) = useState(false)

//     div(
//       Sui.Grid(
//         Sui.GridColumn.width(SemanticWIDTHSSTRING.`1`),
//         Sui.GridColumn.width(SemanticWIDTHSSTRING.`14`)(
//           Sui.Divider.horizontal(true)(
//             Sui.Header.as("h2")(Sui.Icon.name(SemanticICONS.tag), "Button and Icon")
//           ),
//           p(Sui.Button.primary(true)("Primary")),
//           p(Sui.Icon.name(SemanticICONS.recycle)),
//           p(
//             Sui.Button.icon(true)(
//               Sui.Icon.name(SemanticICONS.recycle)
//             )
//           ),
//           p(
//             Sui.Button
//               .labelPosition(left)
//               .icon(true)(
//                 Sui.Icon.name(SemanticICONS.pause),
//                 "Pause"
//               )
//           ),
//           Sui.Divider.horizontal(true)(
//             Sui.Header.as("h4")(
//               Sui.Icon.name(SemanticICONS.tag),
//               "Form and Checkbox"
//             )
//           ),
//           Sui.Form(
//             Sui.FormField(
//               label("First Name"),
//               input(placeholder := "First Name")
//             ),
//             Sui.FormField(
//               label("Last Name"),
//               input(placeholder := "Last Name")
//             ),
//             Sui.FormField(
//               Sui.Checkbox.labelReactElement("I agree to the Terms and Conditions")
//             ),
//             Sui.FormField(
//               Sui.Checkbox
//                 .labelReactElement("I agree to the Cookie Policy")
//                 .toggle(true)
//             ),
//             Sui.Button.`type`(submit)("OK!")
//           ),
//           Sui.Divider.horizontal(true)(
//             Sui.Header.as("h4")(Sui.Icon.name(SemanticICONS.tag), "Card and Image")
//           ),
//           Sui.Card(
//             Sui.Image
//               .size(SemanticSIZES.medium)
//               .wrapped(true)
//               .ui(false)
//               .set("src", "https://react.semantic-ui.com/images/avatar/large/matthew.png"),
//             Sui.CardContent(
//               Sui.CardHeader("Matthew"),
//               Sui.CardMeta(span(className := "date")("Joined in 2015")),
//               Sui.CardDescription("Matthew is a musician living in Nashville.")
//             ),
//             Sui.CardContent.extra(true)(
//               a(Sui.Icon.name(SemanticICONS.user), "22 Friends")
//             )
//           ),
//           Sui.Divider.horizontal(true)(
//             Sui.Header.as("h4")(Sui.Icon.name(SemanticICONS.tag), "Modal")
//           ),
//           p(Sui.Button.primary(true).onClick((_, _) => updateIsModalVisible(true))("Show modal"))
//         ),
//         Sui.GridColumn.width(SemanticWIDTHSSTRING.`1`)
//       ),
//       Sui.Modal
//         .onClose((_, _) => updateIsModalVisible(false))
//         .open(isModalVisible)(
//           Sui.ModalHeader("Select a Photo"),
//           Sui.ModalContent.image(true)(
//             Sui.Image
//               .size(SemanticSIZES.medium)
//               .fluid(true)
//               .wrapped(true)
//               .set("src", "https://react.semantic-ui.com/images/avatar/large/rachel.png"),
//             Sui.ModalDescription(
//               Sui.Header("Default Profile Image"),
//               p("We've found the following gravatar image associated with your e-mail address."),
//               p("Is it okay to use this photo?")
//             )
//           )
//         )
//     )
//   }
// }

// @react object Header {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>
//     header(
//       div(className := "blue-grey lighten-5")(
//         ul(id := "slide-out", className := "sidenav fixed blue-grey lighten-5")(
//           li(className := "no-padding")(
//             "hi"
//             // { Devices.views.collapsibleList.bind }
//           ),
//           li(className := "no-padding")(
//             "hi"
//             // { Apps.views.collapsibleList.bind }
//           ),
//           li(className := "no-padding")(
//             "hihi"
//             // { Mappings.views.collapsibleList.bind }
//           )
//         ),

//         a(id := "menu-button-left", href := "#",  data-"target" := "slide-out", className := "sidenav-trigger button-collapse hide-on-large-only")( i(className := "material-icons")("menu") )
//       )
//     )
//   }
// }

// @react object Content {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>
//     main(
//       div(className := "blue-grey darken-4")(
//         // { CodeEditor.views.main.bind }
//         // <!--{ ConsoleWindow.views.main.bind }-->
//       )
//     )
//   }
// }

// @react object Footer {
//   case class Props()

//   val component = FunctionalComponent[Props] { props =>
//     div(className := "page-footer")(
//       div(className := "container")(
//         div(className := "row")(
//           div(className := "col l6 s12")(
//           ),
//           div(className := "col l4 offset-l2 s12")(

//           ),
//         )
//       ),
//       div(className := "footer-copyright")(
//         div(className := "container")(
//           "AlloSphere Device Server",
//           a(className :="grey-text text-lighten-4 right", href := "#!")("More Services")
//         )
//       )
//     ) 
//   }
// }





  
//   // @html def render(): NodeBinding[HTMLDivElement] = {
//   //   // <header>{ renderHeader.bind }</header>
//   //   // <main>{ renderMain.bind }</main>
//   //   // <footer>{ renderFooter.bind }</footer>
//   //   <div>{"hi"}</div>
//   // }

//   // @html
//   // def renderHeader = {
//   //   <div class="blue-grey lighten-5">
//   //     <ul id="slide-out" class="side-nav fixed blue-grey lighten-5">
//   //       <li class="no-padding">
//   //         { Devices.views.collapsibleList.bind }
//   //       </li>
//   //       <li class="no-padding">
//   //         { Apps.views.collapsibleList.bind }
//   //       </li> 
//   //       <li class="no-padding">
//   //         { Mappings.views.collapsibleList.bind }
//   //       </li>
//   //     </ul>

//   //     <a id="menu-button-left" href="#" data:data-activates="slide-out" class="button-collapse hide-on-large-only"><i class="material-icons">menu</i></a>
//   //   </div>
//   // }

//   // @html
//   // def renderMain = {
//   //   <div class="blue-grey darken-4">
//   //     <!-- <div class="fixed-action-btn click-to-toggle">
//   //       <a class="btn-floating btn-large red">
//   //         <i class="material-icons">menu</i>
//   //       </a>
//   //       <ul>
//   //         <li><a class="btn-floating red"><i class="material-icons">insert_chart</i></a></li>
//   //         <li><a class="btn-floating yellow darken-1"><i class="material-icons">format_quote</i></a></li>
//   //         <li><a class="btn-floating green"><i class="material-icons">publish</i></a></li>
//   //         <li><a class="btn-floating blue"><i class="material-icons">attach_file</i></a></li>
//   //       </ul>
//   //     </div> -->

//   //     { CodeEditor.views.main.bind }
//   //     // <!--{ ConsoleWindow.views.main.bind }-->
//   //   </div>
//   // }

//   // @html def renderFooter = {
//   //   <div class="page-footer">
//   //     <div class="container">
//   //       <div class="row">
//   //         <div class="col l6 s12">
//   //         </div>
//   //         <div class="col l4 offset-l2 s12">

//   //         </div>
//   //       </div>
//   //     </div>
//   //     <div class="footer-copyright">
//   //       <div class="container">
//   //       AlloSphere Device Server
//   //       <a class="grey-text text-lighten-4 right" href="#!">More Services</a>
//   //       </div>
//   //     </div>
//   //   </div> 
//   // }