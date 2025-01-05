package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {

        User user = new User();
        user.setName(name);
        user.setMobile(mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist();
        artist.setName(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(artistName))
                .findFirst()
                .orElse(null);
        if(artist == null){
            createArtist(artistName);
        }

        Album album = new Album();
        album.setTitle(title);
        albums.add(album);

        artistAlbumMap.putIfAbsent(artist, new ArrayList<>());
        artistAlbumMap.get(artist).add(album);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = albums.stream()
                            .filter(a -> a.getTitle().equals(albumName))
                            .findFirst()
                            .orElseThrow(() -> new Exception("Album does not exist"));

        Song song = new Song();
        song.setTitle(title);
        song.setLength(length);
        songs.add(song);

        albumSongMap.putIfAbsent(album, new ArrayList<>());
        albumSongMap.get(album).add(song);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
//        User user = users.stream()
//                        .filter(a->a.getMobile().equals(mobile))
//                        .findFirst()
//                        .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist();
        playlist.setTitle(title);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = users.stream()
                        .filter(a->a.getMobile().equals(mobile))
                        .findFirst()
                        .orElseThrow(() -> new Exception("User does not exist"));

        List<Song> selectedSong = songs.stream()
                                        .filter(song -> songTitles.contains(song.getTitle()))
                                        .toList();

        if(selectedSong.isEmpty()){
            throw new Exception("No matching songs found");
        }

        Playlist playlist = new Playlist();
        playlist.setTitle(title);
        playlists.add(playlist);

        playlistSongMap.put(playlist, selectedSong);
        playlistListenerMap.putIfAbsent(playlist, new ArrayList<>());
        playlistListenerMap.get(playlist).add(user);

        userPlaylistMap.putIfAbsent(user, new ArrayList<>());
        userPlaylistMap.get(user).add(playlist);

        creatorPlaylistMap.put(user, playlist);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = users.stream().filter(u -> u.getMobile().equals(mobile))
                                    .findFirst()
                                    .orElseThrow(() -> new Exception("User does not exist"));
        Playlist playlist = playlists.stream().filter(p -> p.getTitle().equals(playlistTitle)).findFirst()
                                        .orElseThrow(() -> new Exception("Playlist does not exist"));

        playlistListenerMap.put(playlist, new ArrayList<>());
        if(!playlistListenerMap.get(playlist).contains(user)){
            playlistListenerMap.get(playlist).add(user);
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = users.stream().filter(u -> u.getMobile()
                        .equals(mobile))
                        .findFirst()
                        .orElseThrow(() -> new Exception("User does not exist"));
        Song song = songs.stream().filter(s -> s.getTitle().equals(songTitle))
                            .findFirst()
                            .orElseThrow(() -> new Exception("Song does not exist"));

        songLikeMap.putIfAbsent(song, new ArrayList<>());
        if(!songLikeMap.get(song).contains(user)){
            songLikeMap.get(song).add(user);
        }

        return song;
    }

    public String mostPopularArtist() {
        return artistAlbumMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().stream()
                        .mapToInt(album -> albumSongMap.getOrDefault(album, Collections.emptyList()).stream()
                                .mapToInt(song -> songLikeMap.getOrDefault(song, Collections.emptyList()).size())
                                .sum())
                        .sum()))
                .map(entry -> entry.getKey().getName())
                .orElse(null);
    }

    public String mostPopularSong() {
        return songLikeMap.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(entry -> entry.getKey().getTitle())
                .orElse(null);
    }
}
