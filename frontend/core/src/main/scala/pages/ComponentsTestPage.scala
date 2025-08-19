package client
package pages

import components._ 
import com.raquo.laminar.api.L.{*, given}


object ComponentsTestPage { 

  def apply() = {
    div(
      cls := "bg-white-800 p-4",
    
      // Original simple version
      CollapsibleList(
        header = div("Simple Devices List"),
        items = List(
          div("Item 1"),
          div("Item 2"),
          div("Item 3")
        )
      ),
      
      div(cls := "h-4"), // Spacing
      
      // Flexible version - actions included directly in HTML
      CollapsibleList(
        header = div(
          cls := "flex items-center justify-between w-full",
          span("Devices (Flexible Layout)"),
          span(
            span(cls := "hover:bg-blue-600", "On"),
            span(cls := "hover:bg-red-600", "Off"),
            onClick.stopPropagation --> Observer.empty
          )
          
        ),
        items = List(
          div(
            cls := "flex items-center justify-between w-full",
            span("Projector 1"),
            ButtonGroup(List("Connect", "Settings"))
          ),
          div(
            cls := "flex items-center justify-between w-full",
            span("Projector 2"),
            ButtonGroup(List("Connect", "Settings"))
          ),
          div(
            cls := "flex items-center justify-between w-full",
            span("Audio System"),
            ButtonGroup(List("Connect", "Test"))
          ),
          div("Lighting Controller") // Simple item without actions
        )
      ),
      
      div(cls := "h-4"), // Spacing
      
      // Demo of simplified ButtonGroup widget
      div(
        cls := "space-y-6",
        h3("Simplified ButtonGroup Examples:"),
        
        // Basic button group
        div(
          h4("1. Basic Button Group:"),
          ButtonGroup(
            labels = List("Day", "Week", "Month", "Year"),
            onClicks = List(
              () => println("Day clicked"),
              () => println("Week clicked"),
              () => println("Month clicked"),
              () => println("Year clicked")
            )
          )
        ),
        
        // Toggle group - only one selected at a time
        div(
          h4("2. Toggle Group (Radio-style):"),
          ButtonGroup.toggle(
            labels = List("Day", "Week", "Month", "Year"),
            onSelectionChange = Observer[Int](index => println(s"Selected: $index")),
            selectedIndex = Var(1)
          )
        )
      ),
      
      div(cls := "h-4"), // Spacing
      
      // Demo of Badge component
      div(
        cls := "space-y-6",
        h3("Badge Component Examples:"),
        
        // Basic badges
        div(
          h4("1. Basic Badges:"),
          div(
            cls := "flex flex-wrap gap-2",
            Badge("Default"),
            Badge("Success", "success"),
            Badge("Warning", "warning"),
            Badge("Danger", "danger"),
            Badge("Info", "info")
          )
        ),
        
        // Different sizes
        div(
          h4("2. Different Sizes:"),
          div(
            cls := "flex flex-wrap items-center gap-2",
            Badge("Small", "purple", "sm"),
            Badge("Medium", "purple", "md"),
            Badge("Large", "purple", "lg"),
            Badge("Extra Large", "purple", "xl")
          )
        ),
        
        // All variants
        div(
          h4("3. All Color Variants:"),
          div(
            cls := "flex flex-wrap gap-2",
            Badge("Success", "success"),
            Badge("Warning", "warning"),
            Badge("Danger", "danger"),
            Badge("Info", "info"),
            Badge("Purple", "purple"),
            Badge("Gray", "gray"),
            Badge("Pink", "pink"),
            Badge("Indigo", "indigo")
          )
        ),
        
        // Custom badges
        div(
          h4("4. Custom Styling:"),
          div(
            cls := "flex flex-wrap gap-2",
            Badge.custom("Custom Blue", "bg-blue-500", "text-white"),
            Badge.custom("Custom Green", "bg-green-500", "text-white"),
            Badge.custom("Custom Orange", "bg-orange-500", "text-white")
          )
        ),
        
        // Badges with icons (using simple text icons for demo)
        div(
          h4("5. Badges with Icons:"),
          div(
            cls := "flex flex-wrap gap-2",
            Badge.withIcon("Online", span("●"), "success"),
            Badge.withIcon("Offline", span("○"), "gray"),
            Badge.withIcon("Error", span("⚠"), "danger"),
            Badge.withIcon("Info", span("ℹ"), "info")
          )
        ),
        
        // Badges in context
        div(
          h4("6. Badges in Context:"),
          div(
            cls := "space-y-2",
            div(
              cls := "flex items-center gap-2",
              span("Projector 1"),
              Badge("Connected", "success"),
              Badge("HDMI", "info")
            ),
            div(
              cls := "flex items-center gap-2",
              span("Audio System"),
              Badge("Disconnected", "danger"),
              Badge("Bluetooth", "purple")
            ),
            div(
              cls := "flex items-center gap-2",
              span("Lighting Controller"),
              Badge("Standby", "warning"),
              Badge("DMX", "indigo")
            )
          )
        )
      )
    )
  }
}
