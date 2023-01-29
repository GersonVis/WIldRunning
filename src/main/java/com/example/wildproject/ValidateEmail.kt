package com.example.wildproject

import java.util.regex.Matcher
import java.util.regex.Pattern

class ValidateEmail {
  companion object{
      var pat: Pattern? = null
      var mat: Matcher? = null
      fun isEmail(email:String): Boolean{
          pat = Pattern.compile("[a-zA-Z0-9._-]+@[a-z]{3,}+\\.+[a-z]{2,4}")
          mat = pat!!.matcher(email)
          return  mat!!.find()
      }
  }
}