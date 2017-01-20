package com.tenebras.spero.route.uri

class Segment(val plain: String, val params: Map<String, SegmentParam>) {

    companion object {
        fun fromString(raw: String): Segment {
            val params = mutableMapOf<String, SegmentParam>()

            if (raw.contains('{')) {
                var tmp: String = raw
                var end: Int

                while (tmp.contains('{')) {
                    end = tmp.indexOf('}')

                    val name = tmp.substring(tmp.indexOf('{') + 1, end)

                    params.put(name, SegmentParam.fromString(name))

                    tmp = tmp.substring(end+1)
                }
            }

            return Segment(raw, params)
        }
    }

    fun hasParams(): Boolean {
        return params.size > 0
    }

    fun getParam(name: String): SegmentParam {
        return if( params.containsKey(name) ) params[name]!! else throw IllegalArgumentException()
    }
}