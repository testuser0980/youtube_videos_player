
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'screens/welcome_screen.dart';
import 'screens/playlist_screen.dart';
import 'screens/player_screen.dart';
import 'screens/single_video_screen.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'YT Player',
      theme: ThemeData.dark(),
      routes: {
        '/': (context) => WelcomeScreen(),
        '/playlists': (context) => PlaylistScreen(),
        '/player': (context) => PlayerScreen(),
        '/video': (context) => SingleVideoScreen(),
      },
      initialRoute: '/',
    );
  }
}
