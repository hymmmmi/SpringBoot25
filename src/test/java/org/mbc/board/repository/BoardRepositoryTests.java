package org.mbc.board.repository;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mbc.board.domain.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest //메서드용테스트동작
@Log4j2 //로그용
public class BoardRepositoryTests {
    // 영속성 계층에 테스트용

    @Autowired // 생성자 자동 주입
    private BoardRepository boardRepository;

    @Test
    public void testInsert() {
        //데이터베이스에 데이터 주입(c) 테스트 코드
        IntStream.rangeClosed(1,100).forEach(i -> {
            // i 변수에 1~99까지 100개의 정수를 반복해서 생성
            Board board = Board.builder()
                    .title("제목..."+i)
                    .content("내용..."+i)
                    .writer("user"+(i%10))
                    .build(); //@Builder용 (세터 대신 좀 더 간단하고 기독성 좋게)
               // log.info((board));

            Board result = boardRepository.save(board);
            //
            //
            log.info("게시물 번호 출력 : " + result.getBno() + "게시글 제목 : "+result.getTitle());

        }//foreach문 종료

        ); //intstream 종료
    } //testInset 메서드 종료

    @Test
    public void testSelect() {
        Long bno = 100L; //게시물 번호가 100인 개체를 확인해보자

        Optional<Board> result = boardRepository.findById(bno);
        //Optional 널값이 나올 경우를 대비한 객체

        Board board = result.orElseThrow(); //값이 있으면 넣기

        log.info(bno+ "가 데이터 베이스에 존재합니다");
        log.info(board);
    }// testSelect 메서드 종료

    @Test
    public void testUpdate(){

        Long bno = 100L; // 100번 게시물을 가져와서 수정 후 테스트 종료

        Optional<Board> result = boardRepository.findById(bno); //bno를 찾아서 result에 넣는다.

        Board board = result.orElseThrow();

        board.change("수정테스트 제목","수정테스트 내용");

        boardRepository.save(board); // .save 메서드는 pk값이 없으면 insert, pk 있으면 update함.

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    update
        //        board
        //    set
        //        content=?,
        //        moddate=?,
        //        title=?,
        //        writer=?
        //    where
        //        bno=?
    }

    @Test
    public void testDelete(){

        Long bno = 1L;

        boardRepository.deleteById(bno);
        //             .deleteById(bno) - > delete from board where bno = bno


        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    where
        //        b1_0.bno=?
        //Hibernate:
        //    delete
        //    from
        //        board
        //    where
        //        bno=?
    }

    @Test
    public void testPaging() {
        //.findAll() 는 모든 리스트를 출력하는 메서드 select * from board ;
        // 전체 리스트에 페이징과 정렬 기법도 추가해보자

        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());
        //                                  시작번호 페이지당 데이터 개수
        //                                              번호를 기준으로 내림차순 정렬 !!!

        Page<Board> result = boardRepository.findAll(pageable);
        // 1장에 종이에 board 겍체를 가지고 잇는 결과는 result에 담긴다.
        // page 클래스는 다음 페이지 존재여부, 이전 페이지 존재 여부, 전체 데이터 개수, 등등... 계산을 한다.

        //Hibernate:
        //    select
        //        b1_0.bno,
        //        b1_0.content,
        //        b1_0.moddate,
        //        b1_0.regdate,
        //        b1_0.title,
        //        b1_0.writer
        //    from
        //        board b1_0
        //    order by
        //        b1_0.bno desc
        //    limit
        //        ?, ?
        //Hibernate:
        //    select
        //        count(b1_0.bno) board 전체 리스트 수를 알아옴
        //    from
        //        board b1_0

        log.info("전체 게시물 수 : "+ result.getTotalElements()); //199
        log.info("총 페이지 수 : "+ result.getTotalPages()); //20
        log.info("현재 페이지 번호 : "+ result.getNumber()); //0
        log.info("페이지당 데이터 개수 : "+ result.getSize()); //10
        log.info("다음페이지 여부 : " + result.hasNext()); //t
        log.info("시작페이지 여부 : " + result.isFirst()); //t

        // 콘솔에 결과를 출력해보자.
        List<Board> boardList = result.getContent(); //페이징 처리된 내용을 가져와라

        boardList.forEach(board -> log.info(board));
        //forEach는 인덱스를 사용하지 않고 앞에서부터 객체를 리턴함
        //                board -> log.info(board)
        //                        람다식 1개의 명령어가 잇을 때 좋음.

    } //클래스 종료
}
