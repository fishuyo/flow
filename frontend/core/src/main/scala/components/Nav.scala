
package client
package components

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// @JSImport("resources/MenuIcon.css", JSImport.Default)
// @js.native
// object MenuIconCSS extends js.Object

object Nav {
  def apply() = {
    navTag(className := "fixed top-0 w-screen m-0 z-999 flex justify-between items-center bg-black bg-opacity-75 antialiased",
      div(className := "ml-4 text-white", a(href := "/", className := "no-underline p-4 text-2xl font-light text-gray-200", "embodied worlds")),
      
      div(className := "hidden md:flex gap-0 w-1/2",

        div(className := "group w-full h-full my-auto block text-center align-middle",
          a(href := "/about", className := "no-underline p-4 text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300", "about"),
          div(className := "group-hover:block hidden absolute h-auto w-48 bg-black bg-opacity-50",
            a(href := "/bio", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300", "bio"),
            a(href := "/contact", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300", "contact"),
          )
        ),

        div(className := "group w-full h-full my-auto block text-center align-middle",
          a(href := "/projects", className := "no-underline p-4 text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300", "projects"),
          // div(className := "group-hover:block hidden absolute h-auto w-48 bg-black bg-opacity-50")(
          //   a(href := "/projects/performance", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("performance works"),
          //   a(href := "/projects/installations", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("installation works"),
          //   a(href := "/projects/sound", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("sound works"),
          //   // a(href := "/projects/poetry", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("poetic works"),
          // )
        ),

        div(className := "group w-full h-full my-auto block text-center align-middle", 
          a(href := "/research", className := "no-underline p-4 text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300", "research"),
          div(className := "group-hover:block hidden absolute h-auto w-48 bg-black bg-opacity-50",
            a(href := "/research", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300", "overview"),
            // a(href := "/research/somatics", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("somatic movement"),
            // a(href := "/research/embodied-interaction", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("embodied interaction"),
            // a(href := "/research/immersive-environments", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("immersive environments"),
            // a(href := "/research/audio", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("digital audio"),
            // a(href := "/research/biomimesis", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("biomimetic simulation"),
            // a(href := "/research/visualization", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300")("scientific visualization"),
            a(href := "/research/collaborations", className := "no-underline p-2 text-gray-200 hover:bg-white hover:bg-opacity-50 hover:duration-300", "collaborations"),
          )
        ),
        // a(href := "/research", className := "w-full h-full my-auto block no-underline p-4 text-center align-middle text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300")("research"),

        // a(href := "/process", className := "w-full h-full my-auto block no-underline p-4 text-center align-middle text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300")("process"),

        // a(href := "/contact", className := "w-full h-full my-auto block no-underline p-4 text-center align-middle text-gray-200  hover:bg-white hover:bg-opacity-50 hover:duration-300")("contact"),
      ),

      div(className := "mobile_menu md:hidden",
        input(idAttr := "menu_toggle", `type` := "checkbox"), // changed id to cls to build..XXX
        // label(className := "menu_btn", forAttr := "menu_toggle",
        //   span("")
        // ),

        ul(className := "menu_box",
          li( a(className := "menu_item", href := "/about", "about") ),
          li( a(className := "menu_item", href := "/bio", "bio") ),
          li( a(className := "menu_item", href := "/projects", "projects") ),
          li( a(className := "menu_item", href := "/research", "research") ),
          li( a(className := "menu_item", href := "/collaborations", "collaborations") ),
          // li( a(className := "menu_item", href := "/process")("process") ),
          li( a(className := "menu_item", href := "/contact", "contact") ) 
        )
      ),

    )
  }

}
