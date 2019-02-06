object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}

object ClientType extends Enumeration {
  val BRAND, RETAILER = Value
}

case class Review(id: String, aboutId: String, text: String)

case class Product (id: String, name: String, description: String, client: String)

trait Client {
  def id: String
  def name: String
  def clientType: ClientType.Value

}

case class Brand(id: String, name: String) extends Client{
  override def clientType: ClientType.Value = ClientType.BRAND
}
case class Retailer(id: String, name: String) extends Client{
  override def clientType: ClientType.Value = ClientType.RETAILER
}

trait CatalogEdge {
  def sourceClient: String
  def sourceId: String
  def targetClient: String
  def targetId: String
}

case class ProductEdge(sourceClient: String, sourceId: String, targetClient: String, targetId: String) extends CatalogEdge

trait Character {
  def id: String
  def name: Option[String]
  def friends: List[String]
  def appearsIn: List[Episode.Value]
}


case class Human(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Episode.Value],
  homePlanet: Option[String]) extends Character

case class Droid(
  id: String,
  name: Option[String],
  friends: List[String],
  appearsIn: List[Episode.Value],
  primaryFunction: Option[String]) extends Character

class CharacterRepo {
  import CharacterRepo._

  def getHero(episode: Option[Episode.Value]) =
    episode flatMap (_ ⇒ getHuman("1000")) getOrElse droids.last

  def getHuman(id: String): Option[Human] = humans.find(c ⇒ c.id == id)

  def getDroid(id: String): Option[Droid] = droids.find(c ⇒ c.id == id)
  
  def getHumans(limit: Int, offset: Int): List[Human] = humans.drop(offset).take(limit)
  
  def getDroids(limit: Int, offset: Int): List[Droid] = droids.drop(offset).take(limit)
}
object CharacterRepo {
  val humans = List(
    Human(
      id = "1000",
      name = Some("Luke Skywalker"),
      friends = List("1002", "1003", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1001",
      name = Some("Darth Vader"),
      friends = List("1004"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1002",
      name = Some("Han Solo"),
      friends = List("1000", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None),
    Human(
      id = "1003",
      name = Some("Leia Organa"),
      friends = List("1000", "1002", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Alderaan")),
    Human(
      id = "1004",
      name = Some("Wilhuff Tarkin"),
      friends = List("1001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None)
  )

  val droids = List(
    Droid(
      id = "2000",
      name = Some("C-3PO"),
      friends = List("1000", "1002", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Protocol")),
    Droid(
      id = "2001",
      name = Some("R2-D2"),
      friends = List("1000", "1002", "1003"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Astromech"))
  )
}
object BVRepo {
  val lgProducts = List(
    Product(
      id = "product::lg1",
      name = "65LF6350",
      description = "Full HD 1080p Smart LED TV- 65\" Class (64.5\" Diag)",
      client = "lg"
    ),
    Product(
      id = "product::lg2",
      name = "INFINIA 55LX6500",
      description = "Welcome to the third dimension! The INFINIA LX6500 delivers 3D technology and a whole lot more.",
      client = "lg"
    ),
    Product(
      id = "product::lg3",
      name = "gram-13Z950-A.AA3WU1",
      description = "LG gram 13\" Core i5 Processor Ultra-Slim Laptop",
      client = "lg"
    )
  )
  val searsProducts = List(
    Product(
      id = "product::sears1",
      name = "65LF6350",
      description = "Full HD 1080p Smart LED TV- 65\" Class (64.5\" Diag)",
      client = "sears"
    ),
    Product(
      id = "product::sears2",
      name = "INFINIA 55LX6500",
      description = "Welcome to the third dimension! The INFINIA LX6500 delivers 3D technology and a whole lot more.",
      client = "sears"
    )
  )
  val allProducts = lgProducts ++ searsProducts
  val clients: List[Client] = List(
    Brand(
      id = "lg",
      name = "LG"
    ),
    Retailer(
      id = "sears",
      name = "Sears"
    )
  )
}
