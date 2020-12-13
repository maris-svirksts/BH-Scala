package get.resources

object TypeDefinitions {
  type OptionalList[T] = Option[List[Option[T]]]
}
