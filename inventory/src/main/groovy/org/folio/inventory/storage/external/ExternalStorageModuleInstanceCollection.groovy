package org.folio.inventory.storage.external

import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import org.folio.inventory.domain.Instance
import org.folio.inventory.domain.InstanceCollection

import java.util.regex.Pattern

class ExternalStorageModuleInstanceCollection
  implements InstanceCollection {

  private final Vertx vertx

  def ExternalStorageModuleInstanceCollection(final Vertx vertx) {
    this.vertx = vertx
  }

  @Override
  void add(Instance instance, Closure resultCallback) {
    String location = "http://localhost:9492/inventory-storage/instances"

    def onResponse = { response ->
      response.bodyHandler({ buffer ->
        def responseBody = "${buffer.getString(0, buffer.length())}"

        def createdItem = mapFromJson(new JsonObject(responseBody))

        resultCallback(createdItem)
      })
    }

    Handler<Throwable> onException = { println "Exception: ${it}" }

    def instanceToSend = [:]

    instanceToSend.put("title", instance.title)

    vertx.createHttpClient().requestAbs(HttpMethod.POST, location, onResponse)
      .exceptionHandler(onException)
      .putHeader("X-Okapi-Tenant", "not-blank")
      .end(Json.encodePrettily(instanceToSend))
  }

  @Override
  void findById(String id, Closure resultCallback) {
    String location = "http://localhost:9492/inventory-storage/instances/${id}"

    def onResponse = { response ->
      response.bodyHandler({ buffer ->
        def responseBody = "${buffer.getString(0, buffer.length())}"

        def instanceFromServer = new JsonObject(responseBody)

        def foundInstance = mapFromJson(instanceFromServer)

        resultCallback(foundInstance)
      })
    }

    Handler<Throwable> onException = { println "Exception: ${it}" }

    vertx.createHttpClient().requestAbs(HttpMethod.GET, location, onResponse)
      .exceptionHandler(onException)
      .putHeader("X-Okapi-Tenant", "not-blank")
      .end()
  }


  @Override
  void findAll(Closure resultCallback) {
    String location = "http://localhost:9492/inventory-storage/instances"

    def onResponse = { response ->
      response.bodyHandler({ buffer ->
        def responseBody = "${buffer.getString(0, buffer.length())}"

        JsonArray instances = new JsonArray(responseBody)

        def foundInstances = new ArrayList<Instance>()

        instances.each {
          foundInstances.add(mapFromJson(it))
        }

        resultCallback(foundInstances)
      })
    }

    Handler<Throwable> onException = { println "Exception: ${it}" }

    vertx.createHttpClient().requestAbs(HttpMethod.GET, location, onResponse)
      .exceptionHandler(onException)
      .putHeader("X-Okapi-Tenant", "not-blank")
      .end()
  }

  @Override
  void empty(Closure completionCallback) {
    String location = "http://localhost:9492/inventory-storage/instances"

    def onResponse = { response ->
      response.bodyHandler({ buffer ->
        completionCallback()
      })
    }

    Handler<Throwable> onException = { println "Exception: ${it}" }

    vertx.createHttpClient().requestAbs(HttpMethod.DELETE, location, onResponse)
      .exceptionHandler(onException)
      .putHeader("X-Okapi-Tenant", "not-blank")
      .end()
  }

  @Override
  def findByTitle(String partialTitle, Closure resultCallback) {

    //HACK: Replace by server side implementation
    findAll {
      def results = it.findAll {
        Pattern.compile(
        Pattern.quote(partialTitle),
        Pattern.CASE_INSENSITIVE).matcher(it.title).find()
      }

      resultCallback(results)
    }
  }

  private Instance mapFromJson(JsonObject instanceFromServer) {
    new Instance(
      instanceFromServer.getString("id"),
      instanceFromServer.getString("title"))
  }
}