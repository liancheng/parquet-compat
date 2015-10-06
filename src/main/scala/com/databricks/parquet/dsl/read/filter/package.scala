package com.databricks.parquet.dsl.read

import org.apache.parquet.filter2.compat.FilterCompat
import org.apache.parquet.filter2.predicate.FilterApi._
import org.apache.parquet.filter2.predicate.Operators._
import org.apache.parquet.filter2.predicate.{FilterApi, FilterPredicate}

package object filter {
  val boolean = booleanColumn _
  val int = intColumn _
  val long = longColumn _
  val float = floatColumn _
  val double = doubleColumn _
  val binary = binaryColumn _
  val string = binaryColumn _

  implicit class EqNotEq[T <: Comparable[T]](column: Column[T] with SupportsEqNotEq) {
    def ===(literal: T): Eq[T] = FilterApi.eq(column, literal)
    def !==(literal: T): NotEq[T] = notEq(column, literal)
  }

  implicit class LtGt[T <: Comparable[T]](column: Column[T] with SupportsLtGt) {
    def <(literal: T): Lt[T] = lt(column, literal)
    def <=(literal: T): LtEq[T] = ltEq(column, literal)
    def >(literal: T): Gt[T] = gt(column, literal)
    def >=(literal: T): GtEq[T] = gtEq(column, literal)
  }

  implicit class AndOrNot(self: FilterPredicate) {
    def &&(other: FilterPredicate): FilterPredicate = and(self, other)
    def ||(other: FilterPredicate): FilterPredicate = or(self, other)
    def unary_! : FilterPredicate = not(self)
  }

  implicit def filterPredicateToFilterCompat(predicate: FilterPredicate): FilterCompat.Filter = {
    FilterCompat.get(predicate)
  }
}
