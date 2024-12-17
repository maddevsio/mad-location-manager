
SD_ACC_ABS_SET = 0,
SD_ACC_ABS_GENERATED = 1
SD_GPS_SET = 2
SD_GPS_FILTERED = 3
SD_GPS_GENERATED = 4


def convert_gps_line(line: str) -> str:
    to_delete = ["\n", "latitude=", "longitude=", "speed=", "bearing="]
    for td in to_delete:
        line = line.replace(td, "")
    parts = line.split(" ")
    latitude = parts[4]
    longitude = parts[5]
    altitude = 0  # ignored
    location_error = 0  # ignored
    speed = parts[6]
    azimuth = parts[7]
    speed_err = 0  # ignored

    result = f"{SD_GPS_GENERATED} 0.0:::{latitude} {longitude} {
        altitude} {location_error} {azimuth} {speed} {speed_err}\n"
    return result


def convert_acc_line(line: str) -> str:
    line = line.replace("\n", "")
    line = line.replace(",", "")
    line = line.replace("AccelerometerData(", "").replace(")", "")
    line = line.replace("x=", "").replace("y=", "").replace("z=", "")
    parts = line.split(" ")
    hdr_type = SD_ACC_ABS_GENERATED
    ts = parts[-1]
    x = float(parts[5])
    y = float(parts[6])
    z = float(parts[7])

    result = f"{hdr_type} {ts}:::{x} {y} {z}\n"
    return result


def convert(line: str) -> str:
    if "after" in line:
        return ""  # not interested
    if "before" in line and "accelerometerData" in line:
        return ""  # not interested too
    if "processAcc" in line:
        return convert_acc_line(line)
    if "before" in line and "location" in line:
        return convert_gps_line(line)
    return "unknown"


with open("./track4_conv.txt", "w") as fout:
    with open("./track4.txt", "r") as fin:
        while line := fin.readline():
            if line.strip() == "":
                continue
            converted = convert(line)
            fout.write(converted)
            # print(converted)
            # break
