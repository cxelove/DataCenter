/*Table structure for table `web_config` */
CREATE TABLE IF NOT EXISTS `web_config` (
  `id` int DEFAULT 1,
  `stationid` varchar NOT NULL,
  `key` varchar NOT NULL,
  `mapDisplay` boolean DEFAULT FALSE,
  `listDisplay` boolean DEFAULT TRUE,
  unique KEY `stationidANDkey` (`stationid`, `key`)
);
/*Table structure for table `serv_config` */
CREATE TABLE IF NOT EXISTS `serv_config` (
  `property` varchar NOT NULL,
  `value` varchar DEFAULT '',
  PRIMARY KEY (`property`)
);

/*Table structure for table `qx_reupload` */

CREATE TABLE IF NOT EXISTS `qx_reupload`
(
  `stationId` varchar NOT NULL,
  `obTime`    datetime DEFAULT NULL
) ;

/*Table structure for table `qx_station` */

CREATE TABLE IF NOT EXISTS `qx_station`
(
  `stationid` varchar NOT NULL, /*站点号*/
  `obtime`    datetime   DEFAULT NULL,
  `type`      varchar DEFAULT NULL,
  `alias`     tinytext, /*站点名*/
  `lng`       real       DEFAULT NULL COMMENT '经度',
  `lat`       real       DEFAULT NULL COMMENT '纬度',
  `norealtime`  bool		DEFAULT false,
  `protocol`    varchar ,
  `measure`     varchar ,
  PRIMARY KEY (`stationid`)
);

/*Table structure for table `qx_reupload` */

CREATE TABLE IF NOT EXISTS `qx_latest`
(
  `stationId` varchar NOT NULL,
  `obTime`    datetime DEFAULT NULL,
  `ps` varchar DEFAULT NULL,
  `data` VARCHAR DEFAULT NULL,
  PRIMARY KEY (`stationid`)
) ;