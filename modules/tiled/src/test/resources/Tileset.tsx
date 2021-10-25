<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.5" tiledversion="1.7.2" name="Tileset" tilewidth="16" tileheight="16" tilecount="4" columns="2">
 <properties>
  <property name="1" value="hello"/>
  <property name="2" type="bool" value="true"/>
 </properties>
 <image source="Tileset.png" width="32" height="32"/>
 <tile id="1" type="Tree" probability="1.001">
  <objectgroup draworder="index" id="2">
   <object id="1" x="6.9375" y="6.0625" width="7.75" height="8.75"/>
   <object id="3" x="11.73" y="0.846707" width="2.09436" height="2.48705"/>
   <object id="6" x="2" y="1.125" width="5.5625" height="4.5625">
    <ellipse/>
   </object>
   <object id="9" x="9.92275" y="0.4375">
    <polygon points="0,0 -4.29775,4.03208 -2.38764,11.1007 1.81461,11.25 4.20225,3.78319"/>
   </object>
   <object id="10" x="2" y="0" width="13" height="15"/>
  </objectgroup>
 </tile>
 <wangsets>
  <wangset name="Corners" type="corner" tile="-1">
   <wangcolor name="" color="#ff0000" tile="-1" probability="1"/>
   <wangtile tileid="0" wangid="0,1,0,1,0,1,0,1"/>
   <wangtile tileid="1" wangid="0,1,0,1,0,1,0,1"/>
   <wangtile tileid="2" wangid="0,0,0,0,0,1,0,0"/>
  </wangset>
 </wangsets>
</tileset>
