# SkierClient-part1

How to run the client
- Change the url:
  - Find SkierClient.java, and at line 92 you could change the url using
    setBasePath(...)
- Then hit run. That's it!



## Other
- If there is any request failure:
  - first try run again.
  - If problem still exists, you
    could try changing the number of threads and number of requests sent in one single
    thread()  at line 17-21, some combination you could try:
    - NUM_THREADS_PHASE_2 = 84 & NUM_REQUESTS_PHASE_2 = 1000;


