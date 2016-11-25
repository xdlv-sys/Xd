package xd.fw.job;

import org.springframework.stereotype.Service;

@Service
public class MemoryJob extends BaseJob {

    @Override
    public void doExecute() throws Exception {
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();
        logger.info("memory usage free: {}M total: {}M)",free / 1024 / 1024,total / 1024 / 1024);
    }
}
