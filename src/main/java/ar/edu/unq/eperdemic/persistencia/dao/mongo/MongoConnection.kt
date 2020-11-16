package ar.edu.unq.eperdemic.persistencia.dao.mongo

import com.mongodb.*
import com.mongodb.client.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider

object MongoConnection {
    var client: MongoClient
    var dataBase: MongoDatabase
    var session: ClientSession? = null

    fun startTransaction() {
        try {
            session = client.startSession()
            session?.startTransaction(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build())
        }catch (exception: MongoClientException){
            exception.printStackTrace()
        }
    }

    fun commitTransaction() {
        session?.commitTransaction()
        closeSession()
    }

    fun rollbackTransaction() {
        session?.abortTransaction()
        closeSession()
    }

    fun closeSession(){
        session?.close()
        session = null
    }

    fun <T> getCollection(name:String, entityType: Class<T> ): MongoCollection<T> {
        try{
            dataBase.createCollection(name)
        } catch (exception: MongoCommandException){
            println("Ya existe la coleccion $name")
        }
        return dataBase.getCollection(name, entityType)
    }

    init {
        val codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )
        val uri = System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017")
        val connectionString = ConnectionString(uri)
        val settings = MongoClientSettings.builder()
                .codecRegistry(codecRegistry)
                .applyConnectionString(connectionString)
                .build()
        client = MongoClients.create(settings)
        dataBase = client.getDatabase("epersMongo")
    }
}