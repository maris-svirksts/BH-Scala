package server.resources

object HTMLPages {
  val favicon: String =
    """
      |<link rel="apple-touch-icon" sizes="57x57"        href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-57x57.png">
      |<link rel="apple-touch-icon" sizes="60x60"        href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-60x60.png">
      |<link rel="apple-touch-icon" sizes="72x72"        href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-72x72.png">
      |<link rel="apple-touch-icon" sizes="76x76"        href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-76x76.png">
      |<link rel="apple-touch-icon" sizes="114x114"      href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-114x114.png">
      |<link rel="apple-touch-icon" sizes="120x120"      href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-120x120.png">
      |<link rel="apple-touch-icon" sizes="144x144"      href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-144x144.png">
      |<link rel="apple-touch-icon" sizes="152x152"      href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-152x152.png">
      |<link rel="apple-touch-icon" sizes="180x180"      href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/apple-icon-180x180.png">
      |<link rel="icon" type="image/png" sizes="192x192" href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/android-icon-192x192.png">
      |<link rel="icon" type="image/png" sizes="32x32"   href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/favicon-32x32.png">
      |<link rel="icon" type="image/png" sizes="96x96"   href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/favicon-96x96.png">
      |<link rel="icon" type="image/png" sizes="16x16"   href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/favicon-16x16.png">
      |<link rel="manifest" href="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/manifest.json">
      |<meta name="msapplication-TileColor" content="#ffffff">
      |<meta name="msapplication-TileImage" content="https://www.boutique-homes.com/wp-content/themes/BH2016/assets/front/images/ms-icon-144x144.png">
      |<meta name="theme-color" content="#ffffff">
      |""".stripMargin

  val query: String =
    s"""
      |<!doctype html>
      |<html lang="en">
      |<head>
      |<meta charset="utf-8">
      |<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
      |$favicon
      |<title>Query</title>
      |<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css" integrity="sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2" crossorigin="anonymous">
      |</head>
      |<body>
      |<div class="container">
      |<div class="row">
      |<div class="col">
      |<ul class="nav justify-content-center">
      |<li class="nav-item">
      |<a class="nav-link" href="http://127.0.0.1:8080/json/links.json">Master List (JSON)</a>
      |</li>
      |<li class="nav-item">
      |<a class="nav-link" href="http://127.0.0.1:8080/json/106.json">Owner Example (JSON)</a>
      |</li>
      |</ul>
      |</div>
      |</div>
      |<div class="row"><div class="col-12 p-3"><h3>Query</h3></div></div>
      |<div class="row">
      |<div class="col-md-6 col-sm-12">
      |<form action="http://127.0.0.1:8080/results" method="get">
      |<div class="form-group">
      |<label for="queryField">Filter by Owner Data</label>
      |<select class="form-control" name="queryField" id="queryField">
      |<option value="ID">Owner ID</option>
      |<option value="wp_user_level">User Level</option>
      |</select>
      |</div>
      |<div class="form-group">
      |<label for="comparison">Comparison</label>
      |<select class="form-control" name="comparison" id="comparison">
      |<option value="larger">></option>
      |<option value="largerEqual">>=</option>
      |<option value="equal">==</option>
      |<option value="smaller"><</option>
      |<option value="smallerEqual"><=</option>
      |</select>
      |</div>
      |<div class="form-group">
      |<label for="compareAgainst">Compare Against</label>
      |<input type="text" class="form-control" id="compareAgainst" name="compareAgainst" placeholder="500">
      |</div>
      |<div class="form-group text-right">
      |<input class="btn btn-primary" type="submit" value="Submit">
      |</div>
      |</form>
      |</div>
      |<div class="col-md-6 col-sm-12">
      |<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut ante sit amet neque aliquam commodo. Suspendisse lacus lorem, tristique vitae arcu ac, pulvinar viverra orci. Aliquam erat volutpat. Proin vehicula a arcu a tempus. Aliquam ut elementum urna, sit amet auctor urna. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id augue massa. Vestibulum interdum ipsum sit amet maximus eleifend. Sed ac cursus lacus. Quisque at commodo libero. Nullam feugiat ultrices accumsan. Proin nec nunc ligula. Phasellus imperdiet turpis vel nisl luctus facilisis. Nulla consectetur venenatis risus, ac pharetra ipsum sagittis eget. Sed ac quam felis.</p>
      |<p>Cras vel quam a tortor fringilla fermentum nec vitae ex. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Quisque ac risus felis. Donec dignissim consequat ante, non consectetur lacus. Duis lorem magna, dictum nec accumsan sit amet, viverra in leo. Praesent bibendum felis a elit consectetur tempor. Proin sit amet quam sem. Proin sapien tortor, rhoncus ac mauris in, convallis porta justo. Integer maximus erat nec molestie dictum. Mauris sed tortor quam. In a rutrum tortor. Vivamus rutrum libero velit, quis rutrum est pellentesque et. Donec non justo sollicitudin diam bibendum convallis nec ut nulla. Nullam at magna sed dui malesuada venenatis bibendum aliquam risus. Maecenas mollis, odio vitae pretium vestibulum, turpis mi imperdiet elit, vel mattis odio orci id sem.</p>
      |</div>
      |</div>
      |</div>
      |<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
      |<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx" crossorigin="anonymous"></script>
      |</body>
      |</html>""".stripMargin

  def ReturnData(fileName: String): String =
    s"""
      |<!doctype html>
      |<html lang="en">
      |<head>
      |<meta charset="utf-8">
      |<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
      |$favicon
      |<title>Results</title>
      |<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.22/css/jquery.dataTables.min.css">
      |</head>
      |<body>
      |<table id="results" class="display" style="width:100%">
      |<thead>
      |<tr>
      |<th>Owner ID</th>
      |<th>Owner Email</th>
      |<th>Owner Name</th>
      |</tr>
      |</thead>
      |<tfoot>
      |<tr>
      |<th>Owner ID</th>
      |<th>Owner Email</th>
      |<th>Owner Name</th>
      |</tr>
      |</tfoot>
      |</table>
      |<script type="text/javascript" language="javascript" src="https://code.jquery.com/jquery-3.5.1.js"></script>
      |<script type="text/javascript" language="javascript" src="https://cdn.datatables.net/1.10.22/js/jquery.dataTables.min.js"></script>
      |<script>
      |jQuery(document).ready(function() {
      |jQuery('#results').DataTable( {
      |"ajax": 'http://127.0.0.1:8080/json/$fileName',
      |"deferRender": true
      |} );
      |} );
      |</script>
      |</body>
      |</html>""".stripMargin
}
