DROP TABLE IF EXISTS ProcessorUsage;
DROP TABLE IF EXISTS MemoryUsage;
DROP TABLE IF EXISTS ComputerUsage;
DROP TABLE IF EXISTS WindowUsage;
DROP TABLE IF EXISTS MonitoringDates;
DROP TABLE IF EXISTS Windows;

CREATE TABLE MonitoringDates (
    date_id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL UNIQUE
);

CREATE TABLE ProcessorUsage (
    procusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    date_id INTEGER NOT NULL,
    timestamp TEXT NOT NULL,
    cpu_usage REAL NOT NULL,
    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
);

CREATE TABLE MemoryUsage (
    memusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    date_id INTEGER NOT NULL,
    timestamp TEXT NOT NULL,
    memory_usage_mb REAL NOT NULL,
    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
);

CREATE TABLE ComputerUsage (
    compusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    date_id INTEGER NOT NULL,
    time TEXT NOT NULL,
    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
);

CREATE TABLE Windows (
    window_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE WindowUsage (
    winusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
    date_id INTEGER NOT NULL,
    window_id INTEGER NOT NULL,
    time TEXT NOT NULL,
    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id),
    FOREIGN KEY (window_id) REFERENCES Windows(window_id)
);
