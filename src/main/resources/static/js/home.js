/**
 * 全局变量
 *
 * @type {BMap.Map}
 */
var allMarker = {};           // 存储所有marker id=>marker
var infoBox = null;     // Infobox对象
var latestData = null;         // 最新数据
/**
 * 全局函数
 *
 * @type {BMap.Map}
 */
// 定时任务，定时获取最新值
function timeFormat(d) {
    return new Date(d).Format("yyyy-MM-dd hh:mm");
}

function f() {
    $.ajax({
        url: "./api/getSta",
        type: "post",
        timeout: 5000,
        dataType: "json",
        success: function (data) {
            for (var key in data) {
                if (typeof (allMarker[key]) != "undefined" && allMarker[key]) {
                    var imgOffY = 11;

                    switch (data[key]) {
                        case "STATION_ONLINE":
                            imgOffY = 10;
                            break;
                        case "STATION_OFFLINE":
                            imgOffY = 11;
                            break;
                    }
                    if (allMarker[key].getIcon().imageOffset.height != (0 - imgOffY * 25)) {
                        allMarker[key].setIcon(
                            new BMap.Icon('/static/image/markers.png', new BMap.Size(23, 25), {
                                offset: new BMap.Size(10, 25), // 指定定位位置
                                imageOffset: new BMap.Size(0, 0 - imgOffY * 25) // 设置图片偏移,
                            }));
                    }
                }
            }
            ;
        }
    });
};

// 单击Marker弹出Infobox
function mapMarkClick() {
    var mark = this;
    var label = this.getLabel().content;
    var key = label.split("_")[0];
    $.ajax({
        url: "./api/getLatestById?stationId=" + key,
        type: "post",
        timeout: 5000,
        dataType: "json",
        success: function (data) {
            if (data == null) return;
            var road = "Unknown";

            var html = "<div style='height:auto;padding:5px;'>  <div class='user-map-info-header' style='font-weight:bold ;'>" + label + "</div>" +
                "<div class='one-info-content'>" +
                "<hr/><table>" +
                "<tr><td class='tbtdl'>时间</td><td>：" + (new Date(data['data']['OBTIME'])).Format("yyyy-MM-dd hh:mm") + "</td></tr>" +
                "<tr><td class='tbtdl'>电压</td><td>：" + data['data']['PS'] + " V</td></tr>";
            data['title'] = JSON.parse(data['title']);
            for (var i in data['title']) {
                html += "<tr><td class='tbtdl'>" + data['title'][i].name + "</td><td>：" + data['data']['val'][data['title'][i].key] + " " + data['title'][i].unit + "</td></tr>";
            }
            html += "</table></div>";
            if (infoBox != null) {
                infoBox.close();
                infoBox = null;
            }
            infoBox = new BMapLib.InfoBox(window.map, html, {
                boxStyle: {
                    width: "280px",
                    marginBottom: "20px",
                    marginleft: "6px",
                    backgroundColor: "#eee",
                    opacity: "0.8",
                },
                closeIconUrl: "/static/image/close.png",
                closeIconMargin: "1px 1px 0 0",
            });
            infoBox.open(mark);
        },
        fail: function (data) {
        }
    })
}

// 百度地图API功能
var map = new BMap.Map("map", {enableMapClick: false, minZoom: 1, maxZoom: 15});    // 创建Map实例
// map.setMapStyle({styleJson: data});
map.centerAndZoom(new BMap.Point(117.282699092, 31.8669422607), 12);  // 初始化地图,设置中心点坐标和地图级别
map.enableScrollWheelZoom(true);     // 开启鼠标滚轮缩放

$.ajax({
    url: "./api/getMapLng",
    type: "post",
    timeout: 5000,
    dataType: "json",
    success: function (data) {
        var pointArray = new Array();
        var j = 0;
        var imgOffset = 11;
        var myIcon = new BMap.Icon('/static/image/markers.png', new BMap.Size(23, 25), {
            offset: new BMap.Size(10, 25), // 指定定位位置
            imageOffset: new BMap.Size(0, 0 - imgOffset * 25) // 设置图片偏移,
        });
        $.each(data, function (i, val) {
            var point = new BMap.Point(val["lng"], val["lat"]);
            var marker = new BMap.Marker(point, {icon: myIcon});
            var lab = new BMap.Label(val["STATIONID"] + "_" + val["alias"], {
                offset: new BMap.Size(20, 0)
            });
            // 设置label(标注的样式)
            lab.setStyle({
                fontSize: "14px",
                maxWidth: "none",
                border: "none",
                backgroundColor: "none",
                fontWeight: "bold", // 字体加粗
                // color:"blue",
            });
            marker.setLabel(lab);
            marker.addEventListener("click", mapMarkClick);
            map.addOverlay(marker);    // 增加点
            pointArray[j++] = point;
            allMarker[val["stationid"]] = marker;
        });
        // 让所有点在视野范围内
        map.setViewport(pointArray);
        f();
        setInterval(f, 30000);
    },
});
