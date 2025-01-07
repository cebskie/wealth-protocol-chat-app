# wealth-protocol-chat-app
## Client Side:
- Masuk dan gunakan app dengan identitas (nama dan password)
- Memilih jenis chat (group atau private)
- Gunakan Command untuk mengganti chat group (/group), mengganti private chat(/pc), dan keluar app (/quit)

## Server Side:
- Membuat thread untuk meng-handle client
- Mendengarkan dan menerima client yang ingin bergabung dalam server
- Menerima input dari client untuk melakukan: verifikasi identitas, memuat ruang chat sesuai permintaan client, mengakhirin koneksi client (quit)
- Menyimpan data pengguna server, private chat, group chat, dan riwayat setiap chat
- Menampilkan perbaruan dalam ruang chat kepada client
