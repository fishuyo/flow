package client
package components

import com.raquo.laminar.api.L.{*, given}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Badge {
  // Simple badge with default styling
  def apply(content: String) = {
    span(
      cls := "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800",
      content
    )
  }

  // Badge with variant
  def apply(content: String, variant: String) = {
    val (bgClass, textClass) = variant match {
      case "success" => ("bg-green-100", "text-green-800")
      case "warning" => ("bg-yellow-100", "text-yellow-800")
      case "danger" => ("bg-red-100", "text-red-800")
      case "info" => ("bg-blue-100", "text-blue-800")
      case "purple" => ("bg-purple-100", "text-purple-800")
      case "gray" => ("bg-gray-100", "text-gray-800")
      case "pink" => ("bg-pink-100", "text-pink-800")
      case "indigo" => ("bg-indigo-100", "text-indigo-800")
      case _ => ("bg-blue-100", "text-blue-800")
    }
    
    span(
      cls := s"inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium $bgClass $textClass",
      content
    )
  }

  // Badge with variant and size
  def apply(content: String, variant: String, size: String) = {
    val (bgClass, textClass) = variant match {
      case "success" => ("bg-green-100", "text-green-800")
      case "warning" => ("bg-yellow-100", "text-yellow-800")
      case "danger" => ("bg-red-100", "text-red-800")
      case "info" => ("bg-blue-100", "text-blue-800")
      case "purple" => ("bg-purple-100", "text-purple-800")
      case "gray" => ("bg-gray-100", "text-gray-800")
      case "pink" => ("bg-pink-100", "text-pink-800")
      case "indigo" => ("bg-indigo-100", "text-indigo-800")
      case _ => ("bg-blue-100", "text-blue-800")
    }
    
    val sizeClass = size match {
      case "sm" => "px-2 py-0.5 text-xs"
      case "lg" => "px-3 py-1 text-sm"
      case "xl" => "px-4 py-1.5 text-base"
      case _ => "px-2.5 py-0.5 text-xs"
    }
    
    span(
      cls := s"inline-flex items-center rounded-full font-medium $bgClass $textClass $sizeClass",
      content
    )
  }

  // Badge with custom styling
  def custom(content: String, bgColor: String = "bg-blue-100", textColor: String = "text-blue-800") = {
    span(
      cls := s"inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium $bgColor $textColor",
      content
    )
  }

  // Badge with icon
  def withIcon(content: String, icon: HtmlElement, variant: String = "info") = {
    val (bgClass, textClass) = variant match {
      case "success" => ("bg-green-100", "text-green-800")
      case "warning" => ("bg-yellow-100", "text-yellow-800")
      case "danger" => ("bg-red-100", "text-red-800")
      case "info" => ("bg-blue-100", "text-blue-800")
      case "purple" => ("bg-purple-100", "text-purple-800")
      case "gray" => ("bg-gray-100", "text-gray-800")
      case "pink" => ("bg-pink-100", "text-pink-800")
      case "indigo" => ("bg-indigo-100", "text-indigo-800")
      case _ => ("bg-blue-100", "text-blue-800")
    }
    
    span(
      cls := s"inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium $bgClass $textClass",
      icon,
      span(content)
    )
  }
}
