package org.xenei.geoname;

/*
 The main 'geoname' table has the following fields :

geonameid         : integer id of record in geonames database
name              : name of geographical point (utf8) varchar(200)
asciiname         : name of geographical point in plain ascii characters, varchar(200)
alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
latitude          : latitude in decimal degrees (wgs84)
longitude         : longitude in decimal degrees (wgs84)
feature class     : see http://www.geonames.org/export/codes.html, char(1)
feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
country code      : ISO-3166 2-letter country code, 2 characters
cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
admin3 code       : code for third level administrative division, varchar(20)
admin4 code       : code for fourth level administrative division, varchar(20)
population        : bigint (8 byte int)
elevation         : in meters, integer
dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
timezone          : the timezone id (see file timeZone.txt) varchar(40)
modification date : date of last modification in yyyy-MM-dd format
 */
public class GeoName {
	public String geonameid;
	public String name;
	public String asciiname;
	public String alternatenames;
	public String latitude;
	public String longitude;
	public String feature_class;
	public String feature_code;
	public String country_code;
	public String cc2;
	public String admin1_code;
	public String admin2_code;
	public String admin3_code;
	public String admin4_code;
	public String population;
	public String elevation;
	public String dem;
	public String timezone;
	public String modification_date;

	@Override
	public String toString() {
	    return new StringBuilder()
	    .append( "ID: ").append( geonameid ).append( "\n" )
        .append( "Name:" ).append( name ).append( "\n" )
        .append( "Ascii name: ").append( asciiname ).append( "\n" )
        .append( "Alternate names: ").append( alternatenames ).append( "\n" )
        .append( "Latitude: ").append( latitude ).append( "\n" )
        .append( "Longitude: ").append( longitude ).append( "\n" )
        .append( "Feature class: ").append( feature_class ).append( "\n" )
        .append( "Feature code: ").append( feature_code ).append( "\n" )
        .append( "Country code: ").append( country_code ).append( "\n" )
        .append( "Country code2: ").append( cc2 ).append( "\n" )
        .append( "Admin code1: ").append( admin1_code ).append( "\n" )
        .append( "Admin code2: ").append( admin2_code ).append( "\n" )
        .append( "Admin code3: ").append( admin3_code ).append( "\n" )
        .append( "Admin code4: ").append( admin4_code ).append( "\n" )
        .append( "Population: ").append( population ).append( "\n" )
        .append( "Elevation: ").append( elevation ).append( "\n" )
        .append( "Dem: ").append( dem ).append( "\n" )
        .append( "Timezone: ").append( timezone ).append( "\n" )
        .append( "Modification date: ").append( modification_date).toString()
	    .toString();
	}

	public static class Serde {
    	public GeoName deserialize( String txt )
    	{
    		String[] parts = txt.split( "\t");
    		if (parts.length != 19)
    		{
    		    System.out.println( "too short");
    		}
    		GeoName retval = new GeoName();
    		retval.geonameid = parts[0];
    		retval.name = parts[1];
    		retval.asciiname = parts[2];
    		retval.alternatenames = parts[3];
    		retval.latitude = parts[4];
    		retval.longitude = parts[5];
    		retval.feature_class = parts[6];
    		retval.feature_code = parts[7];
    		retval.country_code = parts[8];
    		retval.cc2 = parts[9];
    		retval.admin1_code = parts[10];
    		retval.admin2_code = parts[11];
    		retval.admin3_code = parts[12];
    		retval.admin4_code = parts[13];
    		retval.population = parts[14];
    		retval.elevation = parts[15];
    		retval.dem = parts[16];
    		retval.timezone = parts[17];
    		retval.modification_date = parts[18];
    		return retval;
    	}

    	public String serialize( GeoName geoname) {
    	    return new StringBuffer( geoname.geonameid ).append( "\t" )
    	            .append( geoname.name ).append( "\t" )
                    .append( geoname.asciiname ).append( "\t" )
                    .append( geoname.alternatenames ).append( "\t" )
                    .append( geoname.latitude ).append( "\t" )
                    .append( geoname.longitude ).append( "\t" )
                    .append( geoname.feature_class ).append( "\t" )
                    .append( geoname.feature_code ).append( "\t" )
                    .append( geoname.country_code ).append( "\t" )
                    .append( geoname.cc2 ).append( "\t" )
                    .append( geoname.admin1_code ).append( "\t" )
                    .append( geoname.admin2_code ).append( "\t" )
                    .append( geoname.admin3_code ).append( "\t" )
                    .append( geoname.admin4_code ).append( "\t" )
                    .append( geoname.population ).append( "\t" )
                    .append( geoname.elevation ).append( "\t" )
                    .append( geoname.dem ).append( "\t" )
                    .append( geoname.timezone ).append( "\t" )
                    .append( geoname.modification_date).toString();
    	}
	}

}
