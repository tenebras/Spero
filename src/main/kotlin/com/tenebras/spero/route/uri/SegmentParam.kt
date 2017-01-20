package com.tenebras.spero.route.uri

class SegmentParam(val name: String) {
    companion object {
        fun fromString(str: String): SegmentParam {
            return SegmentParam(str)
        }
    }
}