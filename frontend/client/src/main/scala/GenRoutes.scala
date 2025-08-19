// package client 

// import scala.quoted.*

// inline def childrenOf[T]: List[String] = ${ childrenOfImpl[T] }

// def childrenOfImpl[T: Type](using Quotes): Expr[List[String]] =
//   import quotes.reflect.*
//   val cls = TypeRepr.of[T].classSymbol.get
//   val childSymbols = cls.children.map(_.name)
//   Expr(childSymbols)