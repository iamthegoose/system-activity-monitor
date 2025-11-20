package ua.nychyk.activitymonitor.repositories;

public class MonitorRepositoryFactory {

    private final DatabaseManager db;

    private final ProcessorRepository processorRepository;
    private final MemoryRepository memoryRepository;
    private final ComputerUsageRepository computerUsageRepository;
    private final WindowRepository windowRepository;
    private final MonitoringDaysRepository monitoringDaysRepository;

    public MonitorRepositoryFactory(String dbFile) {
        this.db = new DatabaseManager(dbFile);

        this.processorRepository = new ProcessorRepository(db);
        this.memoryRepository = new MemoryRepository(db);
        this.computerUsageRepository = new ComputerUsageRepository(db);
        this.windowRepository = new WindowRepository(db);
        this.monitoringDaysRepository = new MonitoringDaysRepository(db);
    }

    public ProcessorRepository getProcessorRepository() {
        return processorRepository;
    }

    public MemoryRepository getMemoryRepository() {
        return memoryRepository;
    }

    public ComputerUsageRepository getComputerUsageRepository() {
        return computerUsageRepository;
    }

    public WindowRepository getWindowRepository() {
        return windowRepository;
    }

    public MonitoringDaysRepository getMonitoringDaysRepository() {
        return monitoringDaysRepository;
    }
}
