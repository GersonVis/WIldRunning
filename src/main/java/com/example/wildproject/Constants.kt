package com.example.wildproject

object Constants {
   const val  INTERVAL_LOCATION = 4
   const val LIMIT_DISTANCE_ACCEPTED_BIKE = 0.04 * INTERVAL_LOCATION
   //LIMIT_DISTANCE_ACCEPTED correcto sería 0.04 (40m)
   //40m son 144km/h. Un ciclista profesional en descenso pudiera alcanzar 130

   const val LIMIT_DISTANCE_ACCEPTED_ROLLERSKATE = 0.035 * INTERVAL_LOCATION
   //LIMIT_DISTANCE_ACCEPTED correcto sería 0.035 (35m)
   //35m son 126km/h.
   //In 21 February, 2016 in Brazil, an Italian skater named Sandro Bovo entered the
   //Guinness Book of World Records by finishing with an average speed of 124.67 km/h
   //in downhill roller skating        https://www.youtube.com/watch?v=zl53mi3kIrA


   const val LIMIT_DISTANCE_ACCEPTED_RUNNING = 0.012 * INTERVAL_LOCATION
   //LIMIT_DISTANCE_ACCEPTED correcto sería 0.012 (12 m)
   //12m son 43.2km/h. Usain Bolt alcanza los 42km/h
}