package org.kframework.meta

import org.kframework.attributes._
import org.kframework.kore.K
import org.kframework.kore.Unapply._
import org.kframework.kore.KORE.Sort

import scala.util.Try

/**
 * Created by cos on 2/6/15.
 */
case class Down(imports: Set[String]) extends (K => Any) {

  import org.kframework.builtin.Sorts.KString

  val AttVal = Sort("AttributeValue")

  def apply(o: K): Any = o match {
    case KToken(`KString`, v) => v
    case KToken(`AttVal`, v) => v
    //    case KApply(KLabel("List"), ks, att) => ks.delegate map apply
    //    case KApply(KLabel("Seq"), ks, att) => ks.delegate map apply
    //    case KApply(KLabel("Set"), ks, att) => ks.delegate map apply toSet
    case t@KApply(KLabel(l), ks) if t.att.contains(ClassFromUp) =>
      val classNameRecoveredFromUp = t.att.get[String](ClassFromUp).get
      Reflection.construct(classNameRecoveredFromUp, ks map { apply _ })

    case KApply(KLabel(l), ks) =>
      val children = ks map { apply _ }

      val namespacesToTry = imports
      val matchingClasses = imports map { _ + "." + l }

      matchingClasses
        .view
        .flatMap { name => Try(Reflection.findObject(name)).toOption }
        .flatMap { o => Try(Reflection.invokeMethod(o, "apply", Seq(children))).toOption }
        .headOption.getOrElse {
        matchingClasses
          .view
          .flatMap { className => Try(Reflection.construct(className, ks map apply)).toOption }
          .headOption
          .getOrElse {
          throw new AssertionError("Could not find a proper constructor for " + l +
            "\n with arguments (" + children.mkString(",") +
            ")\n of types (" + children.map(_.getClass()).mkString(",") +
            ")\n Tried:\n    " +
            matchingClasses.mkString("\n    "))
        }
      }

  }
}
