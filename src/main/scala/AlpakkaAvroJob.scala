import akka.kafka.{ConsumerSettings, Subscriptions}
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer}
import org.apache.avro.specific.{SpecificRecord, SpecificRecordBase}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.scaladsl.{Keep, Sink}
import akka.stream.{ActorMaterializer, Materializer}
import org.apache.avro.Schema
import org.apache.avro.util.Utf8

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object AlpakkaAvroJob extends App {
  val schemaRegistryUrl = "http://localhost:8081"
  val bootstrapServers = "localhost:9092"
  val group = "alpakka-group"
  val topic = "pageviews"
  val kafkaAvroSerDeConfig = Map[String, Any] {
    AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl
  }
  implicit val system: ActorSystem = ActorSystem("consumer-sample")
  implicit val materializer: Materializer = ActorMaterializer()

  val deserializer = new KafkaAvroDeserializer()
  deserializer.configure(kafkaAvroSerDeConfig.asJava, false)
  val kafkaAvroDeserializer = deserializer.asInstanceOf[Deserializer[PageView]]
  val consumerSettings: ConsumerSettings[String, PageView] = {
    ConsumerSettings(system, new StringDeserializer, kafkaAvroDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId(group)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  }
  val done = Consumer
      .committableSource(consumerSettings, Subscriptions.topics(topic))
    .runWith(Sink.foreach({record =>
      println(record.record.value())
    }))
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  done onComplete  {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}

case class PageView(var viewtime: java.lang.Long, var userid: String, var pageid: String) extends SpecificRecordBase {
  override def getSchema: Schema = PageView.SCHEMA$

  override def get(field: Int): AnyRef = {
    field match {
      case 0 => viewtime
      case 1 => userid
      case 2 => pageid
    }
  }

  override def put(field: Int, value: Any): Unit = {
    field match {
      case 0 => viewtime = value.asInstanceOf[java.lang.Long]
      case 1 => userid = asString(value)
      case 2 => pageid = asString(value)
    }
  }
  private def asString(v: Any) =
    v match {
      case utf8: Utf8 => utf8.toString
      case _ => v.asInstanceOf[String]
    }
}

object PageView {
  val SCHEMA$ : Schema =
    new org.apache.avro.Schema.Parser().parse("""
                                                |{"namespace": "akka.kafka.testing",
                                                | "type": "record",
                                                | "name": "PageView",
                                                | "fields": [
                                                |     {"name": "viewtime", "type": "long"},
                                                |     {"name": "userid", "type": "string"},
                                                |     {"name": "pageid", "type": "string"}
                                                | ]
                                                |}
                                              """.stripMargin)
}