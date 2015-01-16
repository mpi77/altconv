<?php
/**
 * Index page.
 *
 * @version 1.0
 * @author MPI
 * */

/* init session */
session_start();

date_default_timezone_set('Europe/Prague');

header("Content-Type: application/json; charset=utf-8");

$r = array("ts" => date("Y-m-d H:i:s"), 
            "courses" => array("BTC" => 1.0, 
                                "LTC" => 0.00659, 
                                "DRK" => 0.00595, 
                                "NMC" => 0.00204, 
                                "XRP" => 0.00007));

echo json_encode($r);
exit();
?>
