mkdir -p testinput/3_3-3;
java -classpath .:test.jar:simplelatlng-1.3.0.jar:ejml-0.22.jar:jcoord-1.1-b.jar de.tuhh.luethke.PrePos.Testing.Extract 1 testinput/ testinput/3_3-3/ 3 198 30 3500 10 1700;
