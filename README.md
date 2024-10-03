# 2024년 2학기 3-2 네트워크 프로그래밍

- 기본적으로 멀티스레드 관련 설정은 전부 들어가 있다.
- 별개의 파일이 아니라, `1. Byte Client/Server`에서 시작해 숫자가 커질수록 발전시켜 나가는 형식.

## 1. Byte Client/Server
GUI에서 하나의 바이트 전송/수신  

## 2. Int Client/Server
1을 바꿔서 하나의 바이트가 아니라 온전한 정수 전달. 바이트/정수 변환을 위한 `DataInputStream`, 버퍼 사용을 위한 `BufferedInputStream` 연결.

## 3. Msg Client/Server
2를 바꿔서 행 단위의 문자열 송수신 할 수 있게 변경. 문자열이 되면서 `DataInputStream` 삭제 및 `Reader/Writer`, `BufferedReader/Writer` 등 변경.

## 4. Obj Client/Server
객체를 송수신 할 수 있게 변경.
