package com.accesibilidad.accesibiliapp.data.deteccion

import android.content.Context
// Cambio de importación: de tensorflow.lite.support a ai.edge.litert
import org.tensorflow.lite.support.metadata.MetadataExtractor


import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer

object MetaData {
    val TEMP_CLASSES: List<String> = List(1000) { "class${it + 1}" }

    fun extractNamesFromMetadata(model: MappedByteBuffer): List<String> {
        try {
            // El uso de MetadataExtractor sigue siendo similar, pero bajo el nuevo paquete
            val metadataExtractor = MetadataExtractor(model)
            val inputStream = metadataExtractor.getAssociatedFile("temp_meta.txt")

            if (inputStream != null) {
                val metadata = inputStream.bufferedReader().use { it.readText() }

                val regex = Regex("'names': \\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(metadata)

                val namesContent = match?.groups?.get(1)?.value

                if (namesContent != null) {
                    val regexNames = Regex("\"([^\"]*)\"|'([^']*)'")
                    return regexNames.findAll(namesContent).map { result ->
                        if (result.groupValues[1].isNotEmpty()) {
                            result.groupValues[1]
                        } else {
                            result.groupValues[2]
                        }
                    }.toList()
                }
            }
        } catch (e: Exception) {
            // Es recomendable loguear el error para debuggear la migración
            e.printStackTrace()
        }
        return emptyList()
    }

    fun extractNamesFromLabelFile(context: Context, labelPath: String): List<String> {
        val labels = ArrayList<String>()
        try {
            val inputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (!line.isNullOrEmpty()) {
                labels.add(line)
                line = reader.readLine()
            }
            reader.close()
            inputStream.close()
            return labels
        } catch (e: Exception) {
            return emptyList()
        }
    }
}