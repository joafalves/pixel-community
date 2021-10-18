<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.5" tiledversion="1.7.2" name="Tileset" tilewidth="16" tileheight="16" tilecount="4" columns="2">
 <properties>
  <property name="1" value="hello"/>
  <property name="2" type="bool" value="true"/>
 </properties>
 <image source="Tileset.png" width="32" height="32"/>
 <tile id="0"/>
 <tile id="3"/>
 <tile id="2"/>
 <tile id="1" type="Tree" probability="1.001">
  <objectgroup draworder="index" id="2">
   <object id="1" x="4.5" y="6.25" width="7.75" height="8.75"/>
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
