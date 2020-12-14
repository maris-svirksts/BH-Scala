package json.resources

object TypeDefinitions {
  type OptionalList[T] = Option[List[Option[T]]]
}
