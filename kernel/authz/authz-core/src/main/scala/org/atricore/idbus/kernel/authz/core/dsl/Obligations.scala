package org.atricore.idbus.kernel.authz.core.dsl

/*
 * Atricore IDBus
 *
 * Copyright (c) 2009-2012, Atricore Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import com.escalatesoft.subcut.inject.{BindingModule, Injectable, MutableBindingModule, NewBindingModule}

import collection.mutable.{ListBuffer, Stack}
import org.atricore.idbus.kernel.authz.core.{AttributeAssignment, Obligation}

/**
 * Second-level/Auxiliary DSL for specifying authorization obligations.
 *
 * An obligation is a directive from the Policy Decision Point (PDP) to the Policy Enforcement Point (PEP) on what
 * must be carried out before or after an access is granted.
 * If the PEP is unable to comply with the directive, the granted access may or must not be realized.
 * The augmentation of obligations eliminates a gap between formal requirements and policy enforcement.
 * Obligations can be an effective way to meet formal requirements (non-repudiation for example) that can be hard to
 * implement as access control rules. Furthermore, any formal requirements will be part of the access control policy as
 * obligations and not as separate functions, which makes policies consistent and centralization of the IT environment
 * easier to achieve.                  *
 *
 * @author <a href="mailto:gbrigandi@atricore.org">Gianluca Brigandi</a>
 */
class Obligations {
  var parent: Option[Obligations] = None
  var obligationDeclarations = ListBuffer.empty[ObligationDeclaration]
  var attributeDeclarations = ListBuffer.empty[AttributeDeclaration]
  var current = Stack(this)

  private def newObligationDeclaration(element: ObligationElement): ObligationDeclaration = {
    val obligationDeclaration = new ObligationDeclaration(element)
    obligationDeclaration.parent = Some(this)
    current.top.obligationDeclarations += obligationDeclaration
    current push obligationDeclaration

    obligationDeclaration
  }

  private def newAttributeDeclaration(obligation: ObligationProperty): AttributeDeclaration = {
    val atcl = new AttributeDeclaration(obligation)
    current.top.attributeDeclarations += atcl
    atcl
  }

  def toAST: ObligationsNode = {
    ObligationsNode(obligationDeclarations flatMap {
      _.toObligationNode
    } toList)
  }

  implicit def withObligationDeclaration(value: ObligationElement): ObligationDeclaration = newObligationDeclaration(value)

  implicit def withAttributeDeclaration(value: ObligationProperty): AttributeDeclaration = newAttributeDeclaration(value)
}

class ObligationDeclaration(val element: ObligationElement) extends Obligations {

  def is[T](x: => T) = {
    x;
    parent match {
      case Some(p) => p.current.pop()
      case None =>
    }

  }

  def toObligationNode: List[ObligationNode] = {
    val adecls = attributeDeclarations flatMap {
      _.toObligationPropertyNode
    } toList

    val aasgn =
      obligationDeclarations flatMap {
        o =>
          o.element match {
            case `attributeAssignment` =>
              o.toAttributeAssignmentNode
            case `obligation` => Nil
          }
      } toList

    List(ObligationNode(adecls, aasgn))
  }

  def toAttributeAssignmentNode: List[AttributeAssignmentNode] = {
    List(AttributeAssignmentNode(attributeDeclarations flatMap {
      _.toObligationPropertyNode
    } toList))
  }

}

final class AttributeDeclaration(val property: ObligationProperty) {
  var value: Option[String] = None

  def :=(value: String) = this.value = Some(value)

  def :=(value: Symbol) = this.value = Some(value.name)

  def toObligationPropertyNode = value match {
    case Some(v) => ObligationPropertyNode(property.name, v) :: Nil
    case None => Nil
  }
}


/**
 * Base type for obligation DSL constructs.
 */
trait ObligationElement

case object obligation extends ObligationElement

case object attributeAssignment extends ObligationElement


/**
 * Base type for obligation's properties DSL constructs.
 */
sealed trait ObligationProperty {

  def name: String

  override def toString = name

}

class StringBasedProperty(val name: String) extends ObligationProperty

case object id extends StringBasedProperty("id")

case object category extends StringBasedProperty("category")

case object issuer extends StringBasedProperty("issuer")

case object dataType extends StringBasedProperty("dataType")

case object avalue extends StringBasedProperty("avalue")

case object attributeValue extends StringBasedProperty("attributeValue")

case object fulfillOn extends StringBasedProperty("fulfillOn")


/**
 * Abstract Syntax tree domain abstractions to which DSL statements are transformed to for evaluation.
 */
case class ObligationsNode(obligations: List[ObligationNode])

case class ObligationNode(obligationProperties: List[ObligationPropertyNode], attributeAssignments: List[AttributeAssignmentNode])

case class ObligationPropertyNode(name: String, value: String)

case class AttributeAssignmentNode(properties: List[ObligationPropertyNode])

/**
 * Obligations walker intended to be used within a PEP (Policy Enforcement Point)
 */
class ObligationFulfillment(val obls: List[Obligation], val configuration: ObligationFulfillmentConfig) {

  def fulfill: List[Either[ObligationFulfillmentError, ObligationFulfillmentSuccess]] = {

    obls.map(
      obl => {
        obl.id match {
          case 'urn_oasis_names_tc_xacml_example_obligation_email =>
            runObligation2(
              obl,
              ('urn_oasis_names_tc_xacml_2_0_example_attribute_mailto, 'urn_oasis_names_tc_xacml_2_0_example_attribute_text),
              sendEmail _
            )
        }
      }
    )
  }

  private def runObligation2( obl : Obligation, attrs : Product2[Symbol,Symbol], f : Function2[String,String, Any] ) = {

    val a1: Option[AttributeAssignment] = obl.attributeAssignments.find(_.id == attrs._1)
    val a2: Option[AttributeAssignment] = obl.attributeAssignments.find(_.id == attrs._2)

    (for {
      a1v <- a1
      a2v <- a2
    } yield {
      f.apply(a1v.value, a2v.value) match {
        case Right(outcome) => Right(ObligationFulfillmentSuccess(obl))
        case Left(outcome) => Left(ObligationFulfillmentError(obl))
      }
    }).getOrElse(
      Left(ObligationFulfillmentError(obl))
    )

  }

  private def runObligation3( obl : Obligation, attrs : Product3[Symbol, Symbol, Symbol], f : Function3[String,String,String, Any] ) = {

    val a1: Option[AttributeAssignment] = obl.attributeAssignments.find(_.id == attrs._1)
    val a2: Option[AttributeAssignment] = obl.attributeAssignments.find(_.id == attrs._2)
    val a3: Option[AttributeAssignment] = obl.attributeAssignments.find(_.id == attrs._3)

    (for {
      a1v <- a1
      a2v <- a2
      a3v <- a3
    } yield {
      f.apply(a1v.value, a2v.value, a3v.value) match {
        case Right(outcome) => Right(ObligationFulfillmentSuccess(obl))
        case Left(outcome) => Left(ObligationFulfillmentError(obl))
      }
    }).getOrElse(
      Left(ObligationFulfillmentError(obl))
    )

  }

  private def sendEmail(mailTo: String, text: String): Either[ObligationActionError, ObligationActionSuccess] = {
    println("Sending email : " + mailTo + ", " + text + ", " + configuration.smtpServer)
    Right(ObligationActionSuccess())
  }

}

trait ObligationFulfilmentResult

case class ObligationFulfillmentSuccess(obligation: Obligation) extends ObligationFulfilmentResult

case class ObligationFulfillmentError(obligation: Obligation) extends ObligationFulfilmentResult

trait ObligationActionResult

case class ObligationActionSuccess() extends ObligationActionResult

case class ObligationActionError() extends ObligationActionResult

class ObligationFulfillmentModule(f: MutableBindingModule => Unit) extends NewBindingModule(f)

class ObligationFulfillmentConfig(implicit val bindingModule: BindingModule) extends Injectable {
  val smtpServer = inject [String] ('smtpServer)
}


object ObligationFulfillment {
  def fullfil(obls: List[Obligation], configuration : ObligationFulfillmentConfig ) =
    new ObligationFulfillment(obls, configuration).fulfill
}



