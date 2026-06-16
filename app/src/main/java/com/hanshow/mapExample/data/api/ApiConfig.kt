package com.hanshow.mapExample.data.api

object ApiConfig {
    // Default values - can be overridden from Settings
    var AUTH_BASE_URL: String = "https://showroom1.hanshowcloud.com/"
    var MAP_BASE_URL: String = "https://showroom1.hanshowcloud.com/"

    // Since AUTH_BASE_URL and MAP_BASE_URL share the same value,
    // we use a single "baseUrl" property for convenience
    var baseUrl: String = AUTH_BASE_URL
        set(value) {
            field = value
            AUTH_BASE_URL = value
            MAP_BASE_URL = value
        }

    // Map API headers config
    var CUSTOMER_CODE: String = "hs"
    var STORE_CODE: String = "1802"

    var FLOOR_ID: Int = 23

    var userName: String = "yikai"

    var pwd: String = "aspas0"
}
