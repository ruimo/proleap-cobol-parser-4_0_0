 IDENTIFICATION DIVISION.
 PROGRAM-ID. REWRSTMT.
 PROCEDURE DIVISION.
    SEARCH ALL SOMEDATA1
       VARYING SOMEDATA2
       AT END DISPLAY 'at end'
       WHEN SOMECOND1 NEXT SENTENCE
       WHEN SOMECOND2 DISPLAY 'some cond2'.